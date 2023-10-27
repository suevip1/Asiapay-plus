package com.jeequan.jeepay.pay.channel.qipay;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
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
public class QipayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "七天支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.QIPAY;
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
            String wayCode = normalMchParams.getPayType();
            String subject = "pay";
            String body = "payBody";

            String outTradeNo = payOrder.getPayOrderId();
            long amount = payOrder.getAmount();
            String clientIp = payOrder.getClientIp();
            long reqTime = System.currentTimeMillis();

            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("mchId", mchId);
            map.put("wayCode", wayCode);
            map.put("amount", amount);
            map.put("outTradeNo", outTradeNo);
            map.put("subject", subject);
            map.put("body", body);
            map.put("reqTime", reqTime);
            map.put("clientIp", clientIp);
            map.put("notifyUrl", notifyUrl);
            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("tradeNo");

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
        String key = "70775a3bad1e4d6a93d644f46b134337";

        String mchId = "M1692020617";
        String wayCode = "9002";
        String subject = "俄罗斯";
        String body = "俄罗斯";

        String outTradeNo = RandomStringUtils.random(15, true, true);
        long amount = 20000;
        String clientIp = "127.0.0.1";
        long reqTime = System.currentTimeMillis();

        String notifyUrl = "https://www.test.com";

        map.put("mchId", mchId);
        map.put("wayCode", wayCode);
        map.put("amount", amount);
        map.put("outTradeNo", outTradeNo);
        map.put("subject", subject);
        map.put("body", body);
        map.put("reqTime", reqTime);
        map.put("clientIp", clientIp);
        map.put("notifyUrl", notifyUrl);
        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "https://yangfanpayjm944i.zzbbm.xyz/api/pay/unifiedorder";

//        raw = HttpUtil.post(normalMchParams.getPayGateway(), map,10000);
        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
