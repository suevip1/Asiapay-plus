package com.jeequan.jeepay.pay.channel.doufu;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
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
 * 豆付支付
 */
@Service
@Slf4j
public class DoufuPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[豆付支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.DOUFU;
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

            String merchant_no = normalMchParams.getMchNo();
            String merchant_order_id = payOrder.getPayOrderId();
            String pay_type = normalMchParams.getPayType();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String caller_ip = payOrder.getPayOrderId();
            String trade_name = "下单";
            long timestamp = System.currentTimeMillis();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;

            map.put("merchant_no", merchant_no);
            map.put("merchant_order_id", merchant_order_id);
            map.put("pay_type", pay_type);
            map.put("amount", amount);
            map.put("caller_ip", caller_ip);
            map.put("trade_name", trade_name);
            map.put("timestamp", timestamp);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {

                String payUrl = result.getString("pay_url");
                String passageOrderId = result.getString("platform_order_id");

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
        String key = "0a49f5e82b930a5adda2df29b9aff985";

        String merchant_no = "91913254295";
        String merchant_order_id = RandomStringUtils.random(15, true, true);
        String pay_type = "3";
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String caller_ip = "127.0.0.1";
        String trade_name = "下单";
        long timestamp = System.currentTimeMillis();
        String notify_url = "http://www.test.com";
        String return_url = notify_url;

        map.put("merchant_no", merchant_no);
        map.put("merchant_order_id", merchant_order_id);
        map.put("pay_type", pay_type);
        map.put("amount", amount);
        map.put("caller_ip", caller_ip);
        map.put("trade_name", trade_name);
        map.put("timestamp", timestamp);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);
        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://43.129.246.3/order/pay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();

        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
