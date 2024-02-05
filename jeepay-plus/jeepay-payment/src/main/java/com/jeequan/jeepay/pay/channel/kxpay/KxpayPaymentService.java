package com.jeequan.jeepay.pay.channel.kxpay;

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

@Service
@Slf4j
public class KxpayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[KxPay支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.KXPAY;
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
            String channel = normalMchParams.getPayType();

            String transactionId = payOrder.getPayOrderId();
            Long amount = payOrder.getAmount();
            String callbackUrl = getNotifyUrl(payOrder.getPayOrderId());
            String ip = payOrder.getClientIp();

            map.put("mchId", mchId);
            map.put("channel", channel);
            map.put("transactionId", transactionId);
            map.put("amount", amount);
            map.put("callbackUrl", callbackUrl);
            map.put("ip", ip);

            String signStr = key + "&" + SignatureUtils.getSignContentFilterEmpty(map, null);
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);


            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            raw = HttpUtil.post(payGateway, map, 10000);

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("status").equals("0")) {
                JSONObject data = result.getJSONObject("urls");
                String payUrl = data.getString("orderUrl");
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
        String key = "oxo-2PpUbIKtXcohieEf";

        String mchId = "MCHSMdKzI42";
        String channel = "wechat-big";

        String transactionId = RandomStringUtils.random(15, true, true);
        Long amount = 50000L;
        String callbackUrl = "http://www.test.com";
        String ip = "127.0.0.1";

        map.put("mchId", mchId);
        map.put("channel", channel);
        map.put("transactionId", transactionId);
        map.put("amount", amount);
        map.put("callbackUrl", callbackUrl);
        map.put("ip", ip);

        String signStr = key + "&" + SignatureUtils.getSignContentFilterEmpty(map, null);
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);


        String payGateway = "https://ds-api.tt-pay.tech/api/v1/merchant/orders";

        // 发送POST请求并指定JSON数据
        raw = HttpUtil.post(payGateway, map, 10000);

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}