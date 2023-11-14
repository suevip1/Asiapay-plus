package com.jeequan.jeepay.pay.channel.bolin;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.SignatureUtils;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 铂霖支付
 */
@Service
@Slf4j
public class BolinPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[铂霖支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.BOLIN;
    }

  

    @Override
    public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, PayConfigContext payConfigContext) {
        log.info("[{}]开始下单:{}", LOG_TAG, payOrder.getPayOrderId());
        UnifiedOrderRS res = ApiResBuilder.buildSuccess(UnifiedOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        String raw = "";
        try {
            PayPassage payPassage = payConfigContext.getPayPassage();
            //支付参数转换
            BolinParamsModel bolinParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), BolinParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = bolinParamsModel.getSecret();
            String requestPrivateKey= bolinParamsModel.getRequestPrivateKey();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String pay_memberid = bolinParamsModel.getMchNo();
            String pay_orderid = payOrder.getPayOrderId();
            String pay_applydate = dateFormat.format(new Date());
            String pay_bankcode = bolinParamsModel.getPayType();
            String pay_notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String pay_callbackurl = pay_notifyurl;
            String pay_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());

            map.put("pay_memberid", pay_memberid);
            map.put("pay_orderid", pay_orderid);
            map.put("pay_applydate", pay_applydate);
            map.put("pay_bankcode", pay_bankcode);
            map.put("pay_notifyurl", pay_notifyurl);
            map.put("pay_callbackurl", pay_callbackurl);
            map.put("pay_amount", pay_amount);

            String signValue = JeepayKit.getSign(map, key).toUpperCase();
            String pay_md5sign = SignatureUtils.buildSHA256WithRSASignByPrivateKey(signValue, requestPrivateKey);
            map.put("pay_md5sign", pay_md5sign);
            map.put("type", "json");

            String payGateway = bolinParamsModel.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("status").equals("200")) {

                String payUrl = result.getString("data");
                String passageOrderId = "";

                res.setPayDataType(CS.PAY_DATA_TYPE.PAY_URL);
                res.setPayData(payUrl);

                channelRetMsg.setChannelOrderId(passageOrderId);
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            } else {
                //出码失败
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            }
        } catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR);
            log.error("[{}] 异常: {}", LOG_TAG, payOrder.getPayOrderId());
            log.error(e.getMessage(), e);
        }
        return res;
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "DLbxBrQBIplN2gGTba5CgkT1O4mPR6yJ";
        String rsa = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQClfN3Cuwwrgs56UT1tLbrjiZ643SvA0UJK6nreH0UCrsjGRd/vcs/hW+WKs1rodbIUW1JU7ZJqdzOvzKksqsCFriSWsmoMkijOD/5sFormKPvP3pSczo0/4v07JgsaFLPyziHW/gr08T5u3EYRk2wQi4C+SXHvJVL67yzIalRQvqjvcSP1rJvVqKNnTgUxkx8rtXWjPnYwmKLSb7oULXjkXjBemWDzbNfXOfHy4Cp4Gh4Oo85qtuisaDh7xCuzWquiZ7+7L/Hq+27mveuFxsbBBaVtyEUeoT9xQOkjr3DD1NZbrnQGP0bcvHQ92+RqlDi9zYa/XJg1WNN/YCE0LInbAgMBAAECggEADKaZBVPrQEBFrJAdVAVU7iQNIDC4MYtNuBBoyCY3zlayPoYE8ncicSXnTE0du/uTW38HsjpfIQhvyRaqMCH1icKgRWzqMn/R4r7SQk0O3T1w0pk/XyVjIuklVqo32wyOHReo+ph9GfB/R1ZLfPh6IGGpu+I9dXyy9T/neoK2Y12wYRvPnVJd/Wz3xawqWH41P3Tfi6Ka8wI5teoXgd8D2+FmtSoBKDvLSX0Tf1IM4LVnFItdlQI0Uk+wFeeHzhxMm/hnOEkZI4Tm0gXNECId5q4bTwbru3lqdRnnkJ3zfJbZOZzMkGd3JrlnxGdb7jkl7ZH3XxETt4boG8kCIake4QKBgQDa03W2B3jAhzuvfrnLLrumrJ1yzHA1KAwBSaUaROOE/xS/M1DYAATELFnOYjWyo0Kzq5Sdc0vA9+xdRvLujlAP6E/Yc9hX90sW9vH8YNECJfJaEpo8jH1sNq8YwwhopdROs1PxkP2JOmxfPAUmrWDHP1SEg6awxv65hc2U5WX1MwKBgQDBmcZ0g+uJj3mqf9TqXfjMuQhS7zkEwfluvGkOl3agQP6ArIY+pHaCym94D4OFfKHgOKAZIsKrNFR9nbCNFHr8mDcDJJRVwYI8Zht9Mxrc/WMcFXh+yn5FjytHGimHu1295mKS88YBLPa87KB1TcCrgSKa9oBQQL2omsexrVhIuQKBgGcmmO1GlmBDHxp55UDm9aP66HlN73LPPuF4grBgWtO9nl0G5ov7bkO/GFJWLzPXC8FAF10lFUeQVsYdhLR5NzpYaxMAgLHuq2HW5HjwdnXJx3U4IaZ7H0BcgqtsfFFAIlU66U4M58bBWIaH93Gk9uLVcpTIrlHUbAyuCxuuCyLLAoGBAJ04qmfY8iDmeWGzKk+iPPjUkyP2zhFFDbGWJKMK8fU2kvggI20f1nZku51+y8pN6o/SrIF9LyYl+y+ALe6EYVRcsOBGhogV/BPcwBoutHQe+qvF6/Lbsyv+FqFJZ8yopnofbJH2kihGQaIvcCeTGxKsblP2N0H1MZCJEf8CzR1RAoGBAILCK9hhPkKZUITXaZRmxYgwCQVpfflEcCOie+3605CSqU72AYioxOqfyP6fN9zJEYdfOIeNyHrBpBFVTSX/Vnmq92YnF89n0YfI+c1EkmfSSn1SLuVyXyuYqMhme4S8l1zrVSWltmWk/MkhXeRlnqQQRW1iTAB0nU5+q8XFc5fZ";

        String pay_memberid = "231106447";
        String pay_orderid = RandomStringUtils.random(15, true, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String today = dateFormat.format(new Date());
        String pay_applydate = today;

        String pay_bankcode = "101";
        String pay_notifyurl = "https://www.test.com";
        String pay_callbackurl = pay_notifyurl;
        String pay_amount = AmountUtil.convertCent2Dollar(10000L);

        map.put("pay_memberid", pay_memberid);
        map.put("pay_orderid", pay_orderid);
        map.put("pay_applydate", pay_applydate);
        map.put("pay_bankcode", pay_bankcode);
        map.put("pay_notifyurl", pay_notifyurl);
        map.put("pay_callbackurl", pay_callbackurl);
        map.put("pay_amount", pay_amount);

        String signValue = JeepayKit.getSign(map, key).toUpperCase();
        String pay_md5sign = SignatureUtils.buildSHA256WithRSASignByPrivateKey(signValue, rsa);
        map.put("pay_md5sign", pay_md5sign);
        map.put("type", "json");

        String payGateway = "http://api.bolinpay.cc/pay";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
