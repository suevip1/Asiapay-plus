package com.jeequan.jeepay.pay.channel.mackpay2;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.SignatureUtils;
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
public class Mackpay2PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "MackPay支付2";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.MACKPAY2;
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
            RongFuParamsModel normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), RongFuParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = normalMchParams.getSecret();

            String merchantId = normalMchParams.getMchNo();
            String type = normalMchParams.getPayType();
            String orderId = payOrder.getPayOrderId();

            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = notifyUrl;
            String failUrl = notifyUrl;

            String ip = payOrder.getClientIp();
            String attach = "attach";

            map.put("merchantId", merchantId);
            map.put("type", type);
            map.put("amount", amount);
            map.put("orderId", orderId);
            map.put("notifyUrl", notifyUrl);
            map.put("returnUrl", returnUrl);
            map.put("failUrl", failUrl);
            map.put("ip", ip);
            map.put("attach", attach);


            final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign"}) + "&key=" + key;
            final String sign = SignatureUtils.md5(signContentStr).toLowerCase();

            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            raw = response.body();

            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {

                String payUrl = result.getJSONObject("data").getString("payUrl");
                String passageOrderId = result.getJSONObject("data").getString("orderNo");

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
        String key = "485be16ef429444bad80e09564fb3dfb";

        String merchantId = "rbQbqbZF1-118";
        String type = "713";
        String orderId = RandomStringUtils.random(15, true, true);

        String amount = AmountUtil.convertCent2Dollar(10000L);
        String notifyUrl = "https://www.test.com";
        String returnUrl = notifyUrl;
        String failUrl = notifyUrl;

        String ip = "127.0.0.1";
        String attach = "attach";


        map.put("merchantId", merchantId);
        map.put("type", type);
        map.put("amount", amount);
        map.put("orderId", orderId);
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", returnUrl);
        map.put("failUrl", failUrl);
        map.put("ip", ip);
        map.put("attach", attach);


        final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign"}) + "&key=" + key;
        final String sign = SignatureUtils.md5(signContentStr).toLowerCase();

        map.put("sign", sign);

        String payGateway = "http://154.23.177.10:8080/public/pay/api";
        log.info("[{}]请求报文:{}", LOG_TAG, JSONObject.toJSONString(map));

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}