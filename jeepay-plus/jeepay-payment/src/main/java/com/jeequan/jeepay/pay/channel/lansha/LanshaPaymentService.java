package com.jeequan.jeepay.pay.channel.lansha;

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
public class LanshaPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[蓝鲨支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.LANSHA;
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
            String mcOrderNum = payOrder.getPayOrderId();
            String channelId = normalMchParams.getPayType();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            Long timestamp = System.currentTimeMillis() / 1000;
            String orderAmount = AmountUtil.convertCent2Dollar(payOrder.getAmount());

            map.put("merchantId", merchantId);
            map.put("mcOrderNum", mcOrderNum);
            map.put("channelId", channelId);
            map.put("notifyUrl", notifyUrl);
            map.put("timestamp", timestamp);
            map.put("orderAmount", orderAmount);

            String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toUpperCase();
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
                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("orderNum");

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
        String key = "82cf21c7cbfac20bc6275cb150fb015a";

        String channelId = "QX999981";
        String mcOrderNum = RandomStringUtils.random(15, true, true);
        String merchantId = "758";

        String notifyUrl = "https://www.test.com";
        Long timestamp = System.currentTimeMillis() / 1000;
        String orderAmount = AmountUtil.convertCent2Dollar(10000L);

        map.put("channelId", channelId);
        map.put("mcOrderNum", mcOrderNum);
        map.put("merchantId", merchantId);
        map.put("notifyUrl", notifyUrl);
        map.put("timestamp", timestamp);
        map.put("orderAmount", orderAmount);

        String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toUpperCase();
        map.put("sign", sign);
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));

        // 指定请求体的Content-Type为JSON
        String payGateway = "http://shoppaymcapi10086.sgxyzpay.com/api/order/create";
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}