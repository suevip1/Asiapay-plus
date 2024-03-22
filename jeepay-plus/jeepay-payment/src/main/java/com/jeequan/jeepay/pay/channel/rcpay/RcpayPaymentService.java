package com.jeequan.jeepay.pay.channel.rcpay;

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
public class RcpayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[RC支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.RCPAY;
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

            String time_stamp = String.valueOf(System.currentTimeMillis() / 1000);
            String mch_no = normalMchParams.getMchNo();
            String nonce = RandomStringUtils.random(8, true, false);
            String out_order_no = payOrder.getPayOrderId();
            String amount = payOrder.getAmount().toString();
            String pay_type = normalMchParams.getPayType();
            String client_ip = payOrder.getClientIp();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String currency = "CNY";

            map.put("time_stamp", time_stamp);
            map.put("mch_no", mch_no);
            map.put("nonce", nonce);
            map.put("out_order_no", out_order_no);
            map.put("amount", amount);
            map.put("currency", currency);
            map.put("pay_type", pay_type);
            map.put("client_ip", client_ip);
            map.put("notify_url", notify_url);

            String sign = SignatureUtils.generateSign(map, key);
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("2000")) {
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

    public static void main(String[] args) {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "c97f9d12266f4f1e83d5f23fea92383c";

        String time_stamp = String.valueOf(System.currentTimeMillis() / 1000);
        String mch_no = "1701715951";
        String nonce = RandomStringUtils.random(8, true, false);
        String out_order_no = RandomStringUtils.random(15, true, true);
        String amount = 10000L + "";
        String pay_type = "71";
        String client_ip = "127.0.0.1";
        String currency = "CNY";
        String notify_url = "http://www.test.com";

        map.put("time_stamp", time_stamp);
        map.put("mch_no", mch_no);
        map.put("nonce", nonce);
        map.put("out_order_no", out_order_no);
        map.put("amount", amount);
        map.put("currency", currency);
        map.put("pay_type", pay_type);
        map.put("client_ip", client_ip);
        map.put("notify_url", notify_url);

        String sign = SignatureUtils.generateSign(map, key);
        map.put("sign", sign);

        String payGateway = "https://i.ltyyxfs.xyz/openapi/rc/create";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
