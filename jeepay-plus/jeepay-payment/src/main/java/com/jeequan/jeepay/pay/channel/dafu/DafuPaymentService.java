package com.jeequan.jeepay.pay.channel.dafu;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * xxpay2支付
 */
@Service
@Slf4j
public class DafuPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[大富支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.DAFU;
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

            String mchKey = normalMchParams.getMchNo();
            String mchOrderNo = payOrder.getPayOrderId();
            String product = normalMchParams.getPayType();

            long amount = payOrder.getAmount();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String nonce = RandomStringUtils.random(8, true, false);
            String timestamp = System.currentTimeMillis() + "";

            if (StringUtils.isNotEmpty(payOrder.getClientIp())) {
                map.put("userIp", payOrder.getClientIp());
            }

            map.put("mchKey", mchKey);
            map.put("mchOrderNo", mchOrderNo);
            map.put("amount", amount);

            map.put("product", product);
            map.put("nonce", nonce);
            map.put("timestamp", timestamp);

            map.put("notifyUrl", notifyUrl);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String sign = SignatureUtils.md5(signContent + key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();

            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {

                String payUrl = result.getJSONObject("data").getJSONObject("url").getString("payUrl");
                String passageOrderId = result.getJSONObject("data").getString("serialOrderNo");

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
        String key = "kyszBOE4EB0TfWagJeNyEAI";

        String mchKey = "100377";
        String mchOrderNo = RandomStringUtils.random(15, true, true);
        String product = "1";

        long amount = 10000;
        String notifyUrl = "https://www.test.com";
        String nonce = RandomStringUtils.random(8, true, false);
        String timestamp = System.currentTimeMillis() + "";
        String userIp = "127.0.0.1";

        map.put("mchKey", mchKey);
        map.put("mchOrderNo", mchOrderNo);
        map.put("amount", amount);

        map.put("product", product);
        map.put("nonce", nonce);
        map.put("timestamp", timestamp);
        map.put("userIp", userIp);
        map.put("notifyUrl", notifyUrl);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String sign = SignatureUtils.md5(signContent + key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "https://longpay-api.tianshupay.xyz/api/v1/payment/init";
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
        JSONObject result = JSON.parseObject(raw, JSONObject.class);
        log.info("[{}]请求响应:{}", LOG_TAG, result.toJSONString());
    }
}
