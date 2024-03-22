package com.jeequan.jeepay.pay.channel.sifangkj;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.AmountUtil;
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

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SifangkjpayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[四方科技支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SIFANGKJPAY;
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
            SifangkjpayParamsModel sifangkjpayParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), SifangkjpayParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = sifangkjpayParamsModel.getSecret();

            String pay_osn = payOrder.getPayOrderId();
            String card_type = sifangkjpayParamsModel.getPayType();
            String card_price = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;
            String merchant_id = sifangkjpayParamsModel.getMchNo();
            Long timestamp = System.currentTimeMillis() / 1000;
            String nonce = RandomStringUtils.random(8, true, false);

            map.put("app_secret", key);
            map.put("pay_osn", pay_osn);
            map.put("card_type", card_type);
            map.put("card_price", card_price);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            map.put("merchant_id", merchant_id);
            map.put("timestamp", timestamp);
            map.put("nonce", nonce);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = sifangkjpayParamsModel.getPayGateway();
            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("url");
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
        String key = "65d6f553d4cabalv97ZownhV5KtfymAiI";

        String pay_osn = RandomStringUtils.random(15, true, true);
        String card_type = "22";
        String card_price = AmountUtil.convertCent2Dollar(2000L);
        String notify_url = "https://www.test.com";
        String return_url = notify_url;
        String merchant_id = "202422499066";
        Long timestamp = System.currentTimeMillis() / 1000;
        String nonce = RandomStringUtils.random(8, true, false);

        map.put("app_secret", key);
        map.put("pay_osn", pay_osn);
        map.put("card_type", card_type);
        map.put("card_price", card_price);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);
        map.put("merchant_id", merchant_id);
        map.put("timestamp", timestamp);
        map.put("nonce", nonce);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);
        log.info("[{}]签名串:{}", LOG_TAG, signStr);
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://114.55.34.87/api/square/placeAnOrder";
        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}