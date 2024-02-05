package com.jeequan.jeepay.pay.channel.cypay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
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
public class CypayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[CY支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.CYPAY;
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

            String userName = normalMchParams.getMchNo();
            String channel = normalMchParams.getPayType();

            String orderNum = payOrder.getPayOrderId();
            Long amount = payOrder.getAmount();
            String clientIp = payOrder.getClientIp();
            String device = "device";

            String notify = getNotifyUrl(payOrder.getPayOrderId());
            String timestamp = System.currentTimeMillis() / 1000 + "";

            map.put("userName", userName);
            map.put("channel", channel);
            map.put("orderNum", orderNum);
            map.put("amount", amount);
            map.put("clientIp", clientIp);
            map.put("device", device);
            map.put("notify", notify);
            map.put("timestamp", timestamp);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            raw = HttpUtil.post(payGateway, map, 10000);

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("payAddr");
                String passageOrderId = data.getString("outTradeNo");

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
        String key = "e63aa5ac17364acdb3a0ab835f0ab777";

        String userName = "CY8899";
        String channel = "106";

        String orderNum = RandomStringUtils.random(15, true, true);
        Long amount = 10000L;
        String clientIp = "127.0.0.1";
        String device = "device";

        String notify = "http://www.test.com";
        String timestamp = System.currentTimeMillis() / 1000 + "";


        map.put("userName", userName);
        map.put("channel", channel);
        map.put("orderNum", orderNum);
        map.put("amount", amount);
        map.put("clientIp", clientIp);
        map.put("device", device);
        map.put("notify", notify);
        map.put("timestamp", timestamp);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toUpperCase();
        map.put("sign", sign);


        String payGateway = "http://206.238.114.180:8084/api/pay/create";

        // 发送POST请求并指定JSON数据
        raw = HttpUtil.post(payGateway, map, 10000);

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}