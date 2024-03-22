package com.jeequan.jeepay.pay.channel.xxpay10;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.channel.rongfu.RongFuParamsModel;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;



@Service
@Slf4j
public class Xxpay10PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[xxpay10支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XXPAY10;
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
            RongFuParamsModel rongFuParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), RongFuParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = rongFuParamsModel.getSecret();

            String mchId = rongFuParamsModel.getMchNo();
            String appId = rongFuParamsModel.getAppId();
            String productId = rongFuParamsModel.getPayType();
            String mchOrderNo = payOrder.getPayOrderId();
            String currency = "cny";
            int amount = payOrder.getAmount().intValue();
            String clientIp = payOrder.getClientIp();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = notifyUrl;
            String subject = "subject";
            String body = "body";

            map.put("mchId", mchId);
            map.put("appId", appId);
            map.put("productId", productId);
            map.put("mchOrderNo", mchOrderNo);
            map.put("currency", currency);
            map.put("amount", amount);
            map.put("clientIp", clientIp);
            map.put("notifyUrl", notifyUrl);
            map.put("returnUrl", returnUrl);
            map.put("subject", subject);
            map.put("body", body);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = rongFuParamsModel.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("retCode").equals("SUCCESS")) {

                String payUrl = result.getString("payUrl");
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

    public static void main(String[] args) {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "NORV1UQKMEKBP3UEFKK0F2YYXGGFPGOTJ2QMQFLSPWQGFAQSQSHOPBVJCBK0SP8PCCCPPVAM353KTNAQA1XOVUHLVRDNOZRAR2NRSTLP5TXFZ3IR7FWNKYEP7WYCPMLO";

        String mchId = "20000108";
        String appId = "975948091d194fc9bb8d6e3582e362d2";
        String productId = "8062";
        String mchOrderNo = RandomStringUtils.random(15, true, true);
        String currency = "cny";
        int amount = 20000;
        String clientIp = "127.0.0.1";
        String notifyUrl = "https://www.test.com";
        String returnUrl = notifyUrl;
        String subject = "subject";
        String body = "body";

        map.put("mchId", mchId);
        map.put("appId", appId);
        map.put("productId", productId);
        map.put("mchOrderNo", mchOrderNo);
        map.put("currency", currency);
        map.put("amount", amount);
        map.put("clientIp", clientIp);
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", returnUrl);
        map.put("subject", subject);
        map.put("body", body);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "https://gateway.rongxinlian888.com/api/pay/create_order";

        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}