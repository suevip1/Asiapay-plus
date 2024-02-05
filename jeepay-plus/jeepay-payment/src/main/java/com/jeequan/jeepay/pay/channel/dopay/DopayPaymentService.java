package com.jeequan.jeepay.pay.channel.dopay;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class DopayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "DoPay支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.DOPAY;
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

            String merchant_id = normalMchParams.getMchNo();
            String app_id = normalMchParams.getAppId();
            String pay_type = normalMchParams.getPayType();
            String out_trade_no = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String version = "V2.0";
            String device_type = "wap";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String request_time = dateFormat.format(new Date());

            String nonce_str = RandomStringUtils.random(15, true, true);
            String pay_ip = payOrder.getClientIp();

            map.put("merchant_id", merchant_id);
            map.put("app_id", app_id);
            map.put("amount", amount);
            map.put("pay_type", pay_type);
            map.put("out_trade_no", out_trade_no);
            map.put("notify_url", notify_url);
            map.put("version", version);
            map.put("device_type", device_type);
            map.put("request_time", request_time);
            map.put("nonce_str", nonce_str);
            map.put("pay_ip", pay_ip);

            final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign"}) + key;
            final String sign = SignatureUtils.md5(signContentStr).toUpperCase();

            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            raw = response.body();

            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("status").equals("success")) {

                String payUrl = result.getString("pay_url");
                String passageOrderId = result.getString("trade_no");

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
        String key = "BC42E849E47C468B99F9E45680B9AE80";

        String merchant_id = "201554";
        String app_id = "7FBC1DE3";
        String pay_type = "8001058";
        String out_trade_no = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(30000L);
        String notify_url = "https://www.test.com";
        String version = "V2.0";
        String device_type = "wap";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String request_time = dateFormat.format(new Date());

        String nonce_str = RandomStringUtils.random(15, true, true);
        String pay_ip = "127.0.0.1";

        map.put("merchant_id", merchant_id);
        map.put("app_id", app_id);
        map.put("amount", amount);
        map.put("pay_type", pay_type);
        map.put("out_trade_no", out_trade_no);
        map.put("notify_url", notify_url);
        map.put("version", version);
        map.put("device_type", device_type);
        map.put("request_time", request_time);
        map.put("nonce_str", nonce_str);
        map.put("pay_ip", pay_ip);

        final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign"}) + key;
        final String sign = SignatureUtils.md5(signContentStr).toUpperCase();

        map.put("sign", sign);

        String payGateway = "https://pay3.rain888.net/gateway/dopay";
        log.info("[{}]请求报文:{}", LOG_TAG, JSONObject.toJSONString(map));

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}