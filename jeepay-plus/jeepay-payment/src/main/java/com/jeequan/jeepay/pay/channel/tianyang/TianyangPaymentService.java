package com.jeequan.jeepay.pay.channel.tianyang;

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
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class TianyangPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[tianyang]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.TIANYANG;
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

            String merchantId = normalMchParams.getMchNo();
            String outTradeNo = payOrder.getPayOrderId();
            long amount = payOrder.getAmount();
            String channel = normalMchParams.getPayType();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            long ts = System.currentTimeMillis();

            map.put("merchantId", merchantId);
            map.put("outTradeNo", outTradeNo);
            map.put("amount", amount);
            map.put("channel", channel);
            map.put("notifyUrl", notifyUrl);
            map.put("ts", ts);

            String contentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""}) + "&key=" + key;
            String sign = SignatureUtils.md5(contentStr).toUpperCase();
            map.put("sign", sign);
            log.info("[{}]请求响应:{}", LOG_TAG, map);
            String payGateway = "https://pay.slpay.me/order/create";

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("url");
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
        String key = "0EF3FAD17375EA715B3F514309BDF4A1";

        String merchantId = "20000084";
        String outTradeNo = RandomStringUtils.random(15, true, true);
        long amount = 20000L;
        String channel = "8005";
        String notifyUrl = "https://www.test.com";
        long ts = System.currentTimeMillis();

        map.put("merchantId", merchantId);
        map.put("outTradeNo", outTradeNo);
        map.put("amount", amount);
        map.put("channel", channel);
        map.put("notifyUrl", notifyUrl);
        map.put("ts", ts);

        String contentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""}) + "&key=" + key;
        String sign = SignatureUtils.md5(contentStr).toUpperCase();
        map.put("sign", sign);
        log.info("[{}]请求响应:{}", LOG_TAG, map);
        String payGateway = "https://pay.slpay.me/order/create";

        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}