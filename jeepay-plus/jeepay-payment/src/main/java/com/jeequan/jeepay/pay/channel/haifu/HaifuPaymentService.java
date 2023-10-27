package com.jeequan.jeepay.pay.channel.haifu;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 海富支付
 */
@Service
@Slf4j
public class HaifuPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[海富支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HAIFU;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return "";
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
            NormalMchParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

            Map<String, Object> map = new HashMap<>();
            String key = normalMchParams.getSecret();

            String memberid = normalMchParams.getMchNo();
            String orderid = payOrder.getPayOrderId();
            String payincode = normalMchParams.getPayType();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String returnurl = notifyurl;


            String p8_returnurl = getNotifyUrl(payOrder.getPayOrderId());
            String p9_callbackurl = p8_returnurl;

            map.put("memberid", memberid);
            map.put("orderid", orderid);
            map.put("payincode", payincode);
            map.put("amount", amount);
            map.put("notifyurl", notifyurl);
            map.put("returnurl", returnurl);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("jumpurl");
                String passageOrderId = data.getString("sysorderid");

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


    public static void main(String[] args) {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "5QbC3dGJ8Os0SWpXxTaPwg4rN6HmULqk";

        String memberid = "9513";
        String orderid = RandomStringUtils.random(15, true, true);
        String payincode = "161";
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String notifyurl = "https://www.test.com";
        String returnurl = notifyurl;


        map.put("memberid", memberid);
        map.put("orderid", orderid);
        map.put("payincode", payincode);
        map.put("amount", amount);
        map.put("notifyurl", notifyurl);
        map.put("returnurl", returnurl);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);
        String payGateway = "http://api.hft666.com/v1/pay";
        log.info("[{}]请求Map:{}", LOG_TAG, map);

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}