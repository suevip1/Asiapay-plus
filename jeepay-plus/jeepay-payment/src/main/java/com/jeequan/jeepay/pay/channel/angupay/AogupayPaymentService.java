package com.jeequan.jeepay.pay.channel.angupay;

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

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class AogupayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[AG支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.AOGUPAY;
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

            String account = normalMchParams.getMchNo();
            String order_id = payOrder.getPayOrderId();
            String channel_code = normalMchParams.getPayType();

            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            int money = (int) (payOrder.getAmount() / 100);

            map.put("account", account);
            map.put("order_id", order_id);
            map.put("channel_code", channel_code);
            map.put("notify_url", notify_url);
            map.put("money", money);


            String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSONString(map)).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("pay_url");

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


    public static void main(String[] args) throws Exception {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "qrbmoll9ruqjrq9ywiooefbvpzugveoo";

        String account = "54539708";
        String order_id = RandomStringUtils.random(15, true, true);
        String channel_code = "1003";

        String notify_url = "https://www.test.com";
        int money = (int) (10000L / 100);

        map.put("account", account);
        map.put("order_id", order_id);
        map.put("channel_code", channel_code);
        map.put("notify_url", notify_url);
        map.put("money", money);


        String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));

        // 指定请求体的Content-Type为JSON
        String payGateway = "http://62.234.218.227:10996/api/order/create";
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);

        JSONObject result = JSON.parseObject(raw, JSONObject.class);
        //拉起订单成功
        if (result.getString("code").equals("0")) {
            JSONObject data = result.getJSONObject("data");
            String payUrl = data.getString("payUrl");
            log.info("[{}]请求响应:{}", LOG_TAG, payUrl);
        }
    }

}