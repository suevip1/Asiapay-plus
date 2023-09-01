package com.jeequan.jeepay.pay.channel.hongyun;

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

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class HongyunPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[鸿运支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HONGYUN;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return "";
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

            String appId = normalMchParams.getMchNo();
            String thirdOrderNo = payOrder.getPayOrderId();
            String payType = normalMchParams.getPayType();

            String time = (Long.valueOf(System.currentTimeMillis() / 1000L)) + "";
            int amount = (int) (payOrder.getAmount() / 100);

            String realIp = payOrder.getClientIp();
            String clientType = "1";

            map.put("appId", appId);
            map.put("thirdOrderNo", thirdOrderNo);
            map.put("payType", payType);
            map.put("realIp", realIp);
            map.put("amount", amount);
            map.put("time", time);
            map.put("clientType", clientType);


            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String sign = SignatureUtils.md5(signContent + "&key=" + key).toUpperCase();
            map.put("_sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("result");

                String payUrl = data.getString("payUrl");
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
        String key = "CJY391J4HTSNQTS873UC4G5R39VT8N6JOXSE2Z6XFMQ8RXNU54Z9OD0W0WTLNP9FBQEVR0PGMLPXQF6KIKUOPBLA1DUBME4I17HG5S2ZVCZARK7D7I8IHWC1FWD5HYG0";

        String mchId = "1024";
        String productId = "8009";
        String mchOrderNo = RandomStringUtils.random(15, true, true);
        String currency = "cny";

        String subject = "goods";
        String body = "desc";

        Long amount = 10000L;
        String notifyUrl = "https://www.test.com";

        map.put("mchId", mchId);
        map.put("productId", productId);
        map.put("amount", amount);
        map.put("mchOrderNo", mchOrderNo);

        map.put("currency", currency);
        map.put("subject", subject);
        map.put("body", body);
        map.put("notifyUrl", notifyUrl);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String sign = SignatureUtils.md5(signContent + "&key=" + key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://api.daodao.one/index/pay";

//        // 发送POST请求并指定JSON数据
//        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json") // 指定请求体的Content-Type为JSON
//                .execute();
//        // 处理响应
//        raw = response.body();

        raw = HttpUtil.post(payGateway, map);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}