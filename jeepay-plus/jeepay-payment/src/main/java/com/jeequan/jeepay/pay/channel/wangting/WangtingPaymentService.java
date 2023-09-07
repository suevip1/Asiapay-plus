package com.jeequan.jeepay.pay.channel.wangting;

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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * xxpay2支付
 */
@Service
@Slf4j
public class WangtingPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[三网网厅支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WANGTING;
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
            WangTingParamsModel wangTingParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), WangTingParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = wangTingParamsModel.getSecret();

            String appKey = wangTingParamsModel.getMchNo();
            String orderId = payOrder.getPayOrderId();
            String channelId = wangTingParamsModel.getChannelId();
            String type = wangTingParamsModel.getPayType();
            long amount = payOrder.getAmount();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());


            map.put("orderId", orderId);
            map.put("appKey", appKey);
            map.put("amount", amount);
            map.put("type", type);
            map.put("channelId", channelId);
            map.put("notifyUrl", notifyUrl);

            final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&appSecret=" + key;
            String sign = SignatureUtils.md5(signContentStr).toUpperCase();
            map.put("sign", sign);

            String payGateway = wangTingParamsModel.getPayGateway();

//            raw = HttpUtil.post(payGateway, map);
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json") // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("success")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("businessId");
                ;

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
        String key = "G70U8AO5YMV4W90X1XWWVUXO5MGSVA4W";

        String appKey = "77919189";
        String orderId = RandomStringUtils.random(15, true, true);
        String channelId = "6";
        long amount = 100L;
        String notifyUrl = "https://www.google.com";
        String type = "1";

        map.put("orderId", orderId);
        map.put("appKey", appKey);
        map.put("amount", amount);
        map.put("type", type);
        map.put("channelId", channelId);
        map.put("notifyUrl", notifyUrl);

        final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&appSecret=" + key;
        String sign = SignatureUtils.md5(signContentStr).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://8.130.178.14:8080/morgan/boss/thirdplatform/preOrder";
//        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json") // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
//        raw = HttpUtil.post(payGateway, map);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
