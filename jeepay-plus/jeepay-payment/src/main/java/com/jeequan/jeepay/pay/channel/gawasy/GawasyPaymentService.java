package com.jeequan.jeepay.pay.channel.gawasy;

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

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GawasyPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[Gawasy支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.GAWASY;
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

            String mchId = normalMchParams.getMchNo();
            String currency = "CNY";
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());

            String orderId = payOrder.getPayOrderId();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String channel = normalMchParams.getPayType();

            map.put("mchId", mchId);
            map.put("currency", currency);
            map.put("amount", amount);

            map.put("orderId", orderId);
            map.put("notifyUrl", notifyUrl);
            map.put("channel", channel);

            String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
            String sign = SignatureUtils.md5(SignatureUtils.md5(SignatureUtils.md5(signStr))).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json") // 指定请求体的Content-Type为JSON
                    .execute();
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("payData");

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
        String key = "13d2fd66e1544403828c";

        String mchId = "178f37efb5f444b8bd77";
        String currency = "CNY";
        String amount = AmountUtil.convertCent2Dollar(10000L);

        String orderId = RandomStringUtils.random(15, true, true);
        String notifyUrl = "https://www.test.com";
        String channel = "8016";

        map.put("mchId", mchId);
        map.put("currency", currency);
        map.put("amount", amount);

        map.put("orderId", orderId);
        map.put("notifyUrl", notifyUrl);
        map.put("channel", channel);

        String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
        String sign = SignatureUtils.md5(SignatureUtils.md5(SignatureUtils.md5(signStr))).toUpperCase();
        map.put("sign", sign);

        String payGateway = "https://gws.xdd988.top/export/gaia/bill/create";

//        raw = HttpUtil.post(payGateway, map);
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json") // 指定请求体的Content-Type为JSON
                .execute();
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}