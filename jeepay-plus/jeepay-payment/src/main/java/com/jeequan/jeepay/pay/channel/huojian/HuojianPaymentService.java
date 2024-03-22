package com.jeequan.jeepay.pay.channel.huojian;

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
public class HuojianPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[火箭支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HUOJIAN;
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

            String mch_id = normalMchParams.getMchNo();
            String order_no = payOrder.getPayOrderId();
            String channel = normalMchParams.getPayType();
            String amount = payOrder.getAmount().toString();
            String client_ip = payOrder.getClientIp();
            String subject = "subject";
            String body = "body";
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());

            map.put("mch_id", mch_id);
            map.put("order_no", order_no);
            map.put("channel", channel);
            map.put("amount", amount);
            map.put("client_ip", client_ip);
            map.put("subject", subject);
            map.put("body", body);
            map.put("notify_url", notify_url);
            String signContent = JSON.toJSONString(map);
            String sign = SignatureUtils.md5(signContent + key).toUpperCase();

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json;charset=UTF-8").header("sign", sign).timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("state").equals("ok")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getJSONObject("credential").getString("pay_url");
                String passageOrderId = data.getString("order_no");

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
        String key = "ebac10a900e9ebd54e8cdff0c971c35f";

        String mch_id = "2128566198";
        String order_no = RandomStringUtils.random(15, true, true);
        String channel = "code_apple";
        String amount = "10000";
        String client_ip = "127.0.0.1";
        String subject = "subject";
        String body = "body";
        String notify_url = "https://www.test.com";


        map.put("mch_id", mch_id);
        map.put("order_no", order_no);
        map.put("channel", channel);
        map.put("amount", amount);
        map.put("client_ip", client_ip);
        map.put("subject", subject);
        map.put("body", body);
        map.put("notify_url", notify_url);
        String signContent = JSON.toJSONString(map);
        String sign = SignatureUtils.md5(signContent + key).toUpperCase();

        String payGateway = "http://top.one-top.net/api/v2/charges";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json;charset=UTF-8").header("sign", sign).timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
