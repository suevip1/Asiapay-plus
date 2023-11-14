package com.jeequan.jeepay.pay.channel.pangpang;

import cn.hutool.http.HttpResponse;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 胖胖支付
 */
@Service
@Slf4j
public class PangpangPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "胖胖支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.PANGPANG;
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
            String merchant_no = normalMchParams.getMchNo();
            String pay_num = payOrder.getPayOrderId();
            String pay_method = normalMchParams.getPayType();
            String total_fee = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String clientip = payOrder.getClientIp();
            String merchant_userid = "pangpang";
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String today = dateFormat.format(new Date());
            String return_type = "1";


            map.put("merchant_no", merchant_no);
            map.put("pay_num", pay_num);
            map.put("pay_method", pay_method);
            map.put("total_fee", total_fee);
            map.put("clientip", clientip);
            map.put("merchant_userid", merchant_userid);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            map.put("today", today);
            map.put("return_type", return_type);

            String signContent = merchant_no + pay_num + total_fee + today + key;
            String sign = SignatureUtils.md5(signContent).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map,10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("return_code").equals("1")) {

                String payUrl = result.getString("pay_url");
                String passageOrderId = result.getString("out_trade_no");

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
        String key = "fef36b841d934bada7474f7acc140922";

        String merchant_no = "20068";
        String pay_num = RandomStringUtils.random(15, true, true);

        String pay_method = "8002";
        String total_fee = AmountUtil.convertCent2Dollar(10000L);
        String clientip = "127.0.0.1";
        String merchant_userid = "pangpang";
        String notify_url = "https://www.test.com";
        String return_url = notify_url;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String today = dateFormat.format(new Date());

        String return_type = "1";


        map.put("merchant_no", merchant_no);
        map.put("pay_num", pay_num);
        map.put("pay_method", pay_method);
        map.put("total_fee", total_fee);
        map.put("clientip", clientip);
        map.put("merchant_userid", merchant_userid);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);
        map.put("today", today);
        map.put("return_type", return_type);

        String signContent = merchant_no + pay_num + total_fee + today + key;
        String sign = SignatureUtils.md5(signContent).toUpperCase();
        map.put("sign", sign);
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));


        String payGateway = "http://service-dm9676uk-1320938747.hk.apigw.tencentcs.com/api/createPay";

        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}