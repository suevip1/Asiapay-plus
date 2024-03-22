package com.jeequan.jeepay.pay.channel.rzpay;

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

@Service
@Slf4j
public class RzpayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[RZ支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.RZPAY;
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

            String mch_id = normalMchParams.getMchNo();
            String out_trade_no = payOrder.getPayOrderId();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());

            String pass_code = normalMchParams.getPayType();
            String subject = "subject";
            String body = "body";
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String client_ip = payOrder.getClientIp();

            map.put("mch_id", mch_id);
            map.put("out_trade_no", out_trade_no);
            map.put("timestamp", timestamp);
            map.put("pass_code", pass_code);
            map.put("subject", subject);
            map.put("body", body);
            map.put("notify_url", notify_url);
            map.put("amount", amount);
            map.put("client_ip", client_ip);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {

                String payUrl = result.getJSONObject("data").getString("pay_url");
                String passageOrderId = result.getJSONObject("data").getString("trade_no");

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
        String key = "97c408848f91682c44ed0f7ce4eec59cbf900c6b";

        String mch_id = "80521";
        String out_trade_no = RandomStringUtils.random(15, true, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());

        String pass_code = "szrmb";
        String subject = "subject";
        String body = "body";
        String notify_url = "https://www.test.com";
        String amount = AmountUtil.convertCent2Dollar(50000L);
        String client_ip = "127.0.0.1";

        map.put("mch_id", mch_id);
        map.put("out_trade_no", out_trade_no);
        map.put("timestamp", timestamp);
        map.put("pass_code", pass_code);
        map.put("subject", subject);
        map.put("body", body);
        map.put("notify_url", notify_url);
        map.put("amount", amount);
        map.put("client_ip", client_ip);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);
        log.info("[{}]请求响应:{}", LOG_TAG, map);
        String payGateway = "https://gateway.ruize.app/cashier-api/cashier/unifiedorder";

        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);


    }
}