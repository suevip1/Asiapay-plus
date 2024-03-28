package com.jeequan.jeepay.pay.channel.bichi;

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
public class BichiPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[碧池支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.BICHI;
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
            BichiParamsModel bichiParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), BichiParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = bichiParamsModel.getSecret();

            String merchant_no = bichiParamsModel.getMchNo();
            String out_order_no = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String channel = bichiParamsModel.getChannel();
            String customer_ip = payOrder.getClientIp();
            String pay_type = bichiParamsModel.getPayType();
            String timestamp = System.currentTimeMillis() + "";
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());

            map.put("merchant_no", merchant_no);
            map.put("out_order_no", out_order_no);
            map.put("amount", amount);
            map.put("channel", channel);
            map.put("customer_ip", customer_ip);
            map.put("pay_type", pay_type);
            map.put("notify_url", notify_url);
            map.put("timestamp", timestamp);

            String signStr = amount + channel + customer_ip + merchant_no + notify_url + out_order_no + pay_type + timestamp + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = bichiParamsModel.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("jump_url");
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
        String key = "f9c5156f539b933f7c529ce6a7d41abaeefbbeaa";

        String merchant_no = "10006";
        String out_order_no = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2DollarShort(10000L);
        String channel = "10001";
        String customer_ip = "127.0.0.1";
        String pay_type = "alipay";
        String timestamp = System.currentTimeMillis() + "";
        String notify_url = "http://www.test.com";

        map.put("merchant_no", merchant_no);
        map.put("out_order_no", out_order_no);
        map.put("amount", amount);
        map.put("channel", channel);
        map.put("customer_ip", customer_ip);
        map.put("pay_type", pay_type);
        map.put("notify_url", notify_url);
        map.put("timestamp", timestamp);

        String signStr = amount + channel + customer_ip + merchant_no + notify_url + out_order_no + pay_type + timestamp + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://154.91.64.6:9679/api/pay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}