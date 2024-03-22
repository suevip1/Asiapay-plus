package com.jeequan.jeepay.pay.channel.feicui;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
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
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class FeicuiPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[翡翠支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.FEICUI;
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

            String method = "placeOrder";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());

            String memberId = normalMchParams.getMchNo();
            String callerOrderId = payOrder.getPayOrderId();

            String channelCode = normalMchParams.getPayType();
            Long amount = payOrder.getAmount();
            String merchantCallbackUrl = getNotifyUrl(payOrder.getPayOrderId());

            String clientIp = payOrder.getClientIp();

            map.put("method", method);
            map.put("timestamp", timestamp);
            map.put("memberId", memberId);
            map.put("callerOrderId", callerOrderId);
            map.put("amount", amount);


            map.put("channelCode", channelCode);
            map.put("merchantCallbackUrl", merchantCallbackUrl);
            map.put("clientIp", clientIp);


            String sign = sign(map, key);
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1000")) {
                JSONObject data = result.getJSONObject("data");


                String payUrl = data.getJSONObject("message").getString("url");
                String passageOrderId = data.getString("orderId");

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
        String key = "d61e4ea92c206ee5789f7ad05f98bc81";

        String method = "placeOrder";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());

        String memberId = "229893234";
        String callerOrderId = RandomStringUtils.random(15, true, true);

        String channelCode = "FC6655";
        Long amount = 50000L;
        String merchantCallbackUrl = "https://www.test.com";

        String clientIp = "127.0.0.1";

        map.put("method", method);
        map.put("timestamp", timestamp);
        map.put("memberId", memberId);
        map.put("callerOrderId", callerOrderId);
        map.put("amount", amount);


        map.put("channelCode", channelCode);
        map.put("merchantCallbackUrl", merchantCallbackUrl);
        map.put("clientIp", clientIp);


        String sign = sign(map, key);
        map.put("sign", sign);

        String payGateway = "http://mi.fczhifu.com/optimus/collect/placeOrder";
        log.info("[{}]请求:{}", LOG_TAG, map);
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }


    /**
     * FeicuiPaymentService 签名
     *
     * @param map
     * @param key
     * @return
     */
    public static String sign(Map<String, Object> map, String key) {
        String[] keys = map.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        for (String item : keys) {
            Object value = map.get(item);
            if (StringUtils.hasLength(item)) {
                sb.append(item);
            }
            if (!Objects.isNull(value)) {
                sb.append(value);
            }
        }
        sb.append(key);
        return SignatureUtils.md5(sb.toString()).toLowerCase();
    }
}