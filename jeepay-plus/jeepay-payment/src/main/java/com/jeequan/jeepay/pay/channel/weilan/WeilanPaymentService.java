package com.jeequan.jeepay.pay.channel.weilan;

import cn.hutool.http.HttpResponse;
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

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.*;

@Service
@Slf4j
public class WeilanPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[蔚蓝支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WEILAN;
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

            String AccessKey = normalMchParams.getMchNo();

            String PayChannelId = normalMchParams.getPayType();
            String OrderNo = payOrder.getPayOrderId();
            String Amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());

            String CallbackUrl = getNotifyUrl(payOrder.getPayOrderId());
            Long Timestamp = System.currentTimeMillis() / 1000;

            map.put("Timestamp", Timestamp);
            map.put("AccessKey", AccessKey);
            map.put("PayChannelId", PayChannelId);
            map.put("OrderNo", OrderNo);

            map.put("Amount", Amount);
            map.put("CallbackUrl", CallbackUrl);

            String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&SecretKey=" + key;
            String Sign = SignatureUtils.md5(SignStr).toLowerCase();
            map.put("Sign", Sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSONString(map)).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("Code").equals("0")) {
                JSONObject data = result.getJSONObject("Data");
                String payUrl = data.getJSONObject("PayeeInfo").getString("CashUrl");
                String passageOrderId = data.getString("OrderNo");

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


    public static void main(String[] args) throws Exception {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "027cf39b9519099391b9227922256afb";

        String PayChannelId = "7";
        String OrderNo = RandomStringUtils.random(15, true, true);
        String AccessKey = "356";

        Long Timestamp = System.currentTimeMillis() / 1000;
        BigDecimal Amount = new BigDecimal(AmountUtil.convertCent2Dollar(10000L));
        String CallbackUrl = "https://www.test.com";

        map.put("Timestamp", Timestamp);
        map.put("AccessKey", AccessKey);
        map.put("PayChannelId", PayChannelId);
        map.put("OrderNo", OrderNo);
        map.put("Amount", Amount);
        map.put("CallbackUrl", CallbackUrl);

        String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toUpperCase();
//        String sign = generateMd5(map, key);
//        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));

        // 指定请求体的Content-Type为JSON
        String payGateway = "http://shoppaymerchant1001.sgxyzpay.com/api/order/create";
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }


    public static String generateMd5(Map<String, Object> params, String key) throws Exception {
        // 按照参数名 ASCII 码从小到大排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            // 忽略空值参数
            String value = params.get(k).toString();
            if (value != null && !"".equals(value.trim())) {
                sb.append(k).append("=").append(value).append("&");
            }
        }

        // 拼接上key值
        sb.append("key=").append(key);

        // 计算MD5摘要并返回
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(sb.toString().getBytes("UTF-8"));

        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                result.append("0");
            }
            result.append(hex);
        }
        return result.toString().toUpperCase();
    }

}