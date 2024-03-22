package com.jeequan.jeepay.pay.channel.gulang;

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
public class GulangPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[孤狼支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.GULANG;
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
            GulangParams gulangParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), GulangParams.class);

            Map<String, Object> map = new HashMap<>();
            String key = gulangParams.getSecret();
            String apiKey = gulangParams.getApiKey();

            String mid = gulangParams.getMchNo();
            Long time = System.currentTimeMillis() / 1000;
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String currency = "CNY";
            String order_no = payOrder.getPayOrderId();
            String gateway = gulangParams.getPayType();
            String ip = payOrder.getClientIp();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());

            map.put("mid", mid);
            map.put("time", time);
            map.put("amount", amount);
            map.put("currency", currency);
            map.put("order_no", order_no);
            map.put("gateway", gateway);
            map.put("ip", ip);
            map.put("notify_url", notify_url);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);
            String sign = SignatureUtils.hmacSign(key, signStr);
            map.put("sign", sign);

            String payGateway = gulangParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).header("Authorization", "api-key "+apiKey).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
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
        String key = "4fa59b873d2b2ee3ab7274882625e5a7be04c6d6f536041c12e9ac4196fb";
        String apiKey = "4583f369f4995da85899afd880813435af481faf";

        String mid = "112008";
        Long time = System.currentTimeMillis() / 1000;
        String amount = AmountUtil.convertCent2Dollar(30000L);
        String currency = "CNY";
        String order_no = RandomStringUtils.random(15, true, true);
        String gateway = "mobile_bank";
        String ip = "127.0.0.1";
        String notify_url = "http://www.test.com";

        map.put("mid", mid);
        map.put("time", time);
        map.put("amount", amount);
        map.put("currency", currency);
        map.put("order_no", order_no);
        map.put("gateway", gateway);
        map.put("ip", ip);
        map.put("notify_url", notify_url);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);
        String sign = SignatureUtils.hmacSign(key, signStr);
        map.put("sign", sign);

        String payGateway = "https://api.keepwin88.cc/api/v1/deposits";

        log.info("[{}]请求json:{}", LOG_TAG, JSON.toJSONString(map));
        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).header("Authorization", "api-key "+apiKey).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }


}
