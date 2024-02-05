package com.jeequan.jeepay.pay.channel.huayue;

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
import java.util.Random;

/**
 * 华悦支付
 */
@Service
@Slf4j
public class HuayuePaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[华悦支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HUAYUE;
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
            HuayueParamsModel huayueParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), HuayueParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = huayueParamsModel.getSecret();

            String orderAmount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String orderId = payOrder.getPayOrderId();
            String merchant = huayueParamsModel.getMchNo();
            String payMethod = huayueParamsModel.getPayMethod();
            String payType = huayueParamsModel.getPayType();
            String version = "1.0";
            String signType = "MD5";
            String outcome = "yes";
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            long createTime = System.currentTimeMillis() / 1000;
            String returnUrl = notifyUrl;
            String phone = generateRandomPhoneNumber();

            map.put("orderAmount", orderAmount);
            map.put("orderId", orderId);
            map.put("merchant", merchant);
            map.put("payMethod", payMethod);
            map.put("payType", payType);
            map.put("version", version);
            map.put("signType", signType);
            map.put("outcome", outcome);

            String signContent = SignatureUtils.getSignContent(map, null, null) + key;
            String sign = SignatureUtils.md5(signContent).toUpperCase();
            map.put("sign", sign);
            map.put("notifyUrl", notifyUrl);
            map.put("createTime", createTime);
            map.put("returnUrl", returnUrl);
            map.put("phone", phone);

            String payGateway = huayueParamsModel.getPayGateway();

            raw = HttpUtil.post(payGateway, map,10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("000000")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("jumpUrl");
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
        String key = "CADDC62088AC1C77DB4C198F22AD3662";

        String orderAmount = AmountUtil.convertCent2Dollar(10000L);
        String orderId = RandomStringUtils.random(15, true, true);
        String merchant = "202311230324547965";
        String payMethod = "2";
        String payType = "23";
        String version = "1.0";
        String signType = "MD5";
        String outcome = "yes";
        String notifyUrl = "http://www.test.com";
        long createTime = System.currentTimeMillis() / 1000;
        String returnUrl = notifyUrl;
        String phone = generateRandomPhoneNumber();

        map.put("orderAmount", orderAmount);
        map.put("orderId", orderId);
        map.put("merchant", merchant);
        map.put("payMethod", payMethod);
        map.put("payType", payType);
        map.put("version", version);
        map.put("signType", signType);
        map.put("outcome", outcome);

        String signContent = SignatureUtils.getSignContent(map, null, null) + key;
        String sign = SignatureUtils.md5(signContent).toUpperCase();
        map.put("sign", sign);
        map.put("notifyUrl", notifyUrl);
        map.put("createTime", createTime);
        map.put("returnUrl", returnUrl);
        map.put("phone", phone);

        String payGateway = "http://8.210.117.229/api/add";

        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);

        /*Map<String, Object> map = new HashMap<>();
        String merchant = "202311230324547965";
        String key = "CADDC62088AC1C77DB4C198F22AD3662";
        String orderId = "Wny4M2vhrowIgtM";
        map.put("partner", merchant);
        map.put("orderId", orderId);
        String sign = SignatureUtils.md5(merchant+key+orderId).toLowerCase();
        map.put("sign", sign);
        map.put("isLoop", "no");

        String raw = HttpUtil.get("http://8.210.117.229/api/check", map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);*/
    }

    private static String generateRandomPhoneNumber() {
        Random random = new Random();

        // 生成随机的手机号码前三位（号段）
        String[] phoneSegments = {
                "130", "131", "132", "155", "156", "186", "185",
                "134", "135", "136", "137", "138", "139", "150",
                "151", "152", "157", "158", "159", "182", "183",
                "187", "188", "133", "153", "180", "181", "189"
        };
        String segment = phoneSegments[random.nextInt(phoneSegments.length)];

        // 生成随机的手机号码后八位
        String phoneNumber = String.format("%08d", random.nextInt(100000000));

        return segment + phoneNumber;
    }
}
