package com.jeequan.jeepay.pay.channel.x5pay;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
public class X5payPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[X5支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.X5PAY;
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
            X5payParamsModel x5payParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), X5payParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = x5payParamsModel.getSecret();

            String mchId = x5payParamsModel.getMchNo();
            String billNo = payOrder.getPayOrderId();
            Long totalAmount = payOrder.getAmount();
            String billDesc = "在线支付";
            String way = x5payParamsModel.getPayType();
            String payment = x5payParamsModel.getPayment();
            String ctype = "json";
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = notifyUrl;

            map.put("mchId", mchId);
            map.put("billNo", billNo);
            map.put("totalAmount", totalAmount);
            map.put("billDesc", billDesc);
            map.put("way", way);
            map.put("payment", payment);
            map.put("ctype", ctype);
            map.put("notifyUrl", notifyUrl);
            map.put("returnUrl", returnUrl);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = x5payParamsModel.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("result");

                String payUrl = data.getString("linkUrl");
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
        String key = "d2547df0ce57bf9e7aae42c4ff11f33b3bc93372479594d8cbb1e7dc7c162e3b";

        String mchId = "600600297";
        String billNo = RandomStringUtils.random(15, true, true);
        String totalAmount = "10000";
        String billDesc = "在线支付";
        String way = "wap";
        String payment = "alipay";
        String ctype = "json";
        String notifyUrl = "http://www.test.com";
        String returnUrl = notifyUrl;

        map.put("mchId", mchId);
        map.put("billNo", billNo);
        map.put("totalAmount", totalAmount);
        map.put("billDesc", billDesc);
        map.put("way", way);
        map.put("payment", payment);
        map.put("ctype", ctype);
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", returnUrl);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://pay.x5vip.live/game/UnifiedorderAsync";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
