package com.jeequan.jeepay.pay.channel.fengye;

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

/**
 * 枫叶支付
 */
@Service
@Slf4j
public class FengyePaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[枫叶支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.FENGYE;
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

            String NotifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String ReturnUrl = NotifyUrl;
            String OrderId = payOrder.getPayOrderId();
            String Amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String PayType = normalMchParams.getPayType();
            String Method = "orderCreate";
            String MerchantId = normalMchParams.getMchNo();

            map.put("NotifyUrl", NotifyUrl);
            map.put("ReturnUrl", ReturnUrl);
            map.put("OrderId", OrderId);
            map.put("Amount", Amount);
            map.put("PayType", PayType);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String Sign = SignatureUtils.md5(signContent + "&key=" + key).toUpperCase();
            map.put("Sign", Sign);
            map.put("Method", Method);
            map.put("MerchantId", MerchantId);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("PayUrl");
                String passageOrderId = data.getString("TradeNo");

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
        String key = "8835D5B7DFBE7FEF3797BB609980F56D";

        String Method = "orderCreate";
        String MerchantId = "300677";
        String NotifyUrl = "http://www.test.com";
        String ReturnUrl = NotifyUrl;
        String OrderId = RandomStringUtils.random(15, true, true);
        String Amount = AmountUtil.convertCent2Dollar(5000L);
        String PayType = "SqGmmApp";

        map.put("NotifyUrl", NotifyUrl);
        map.put("ReturnUrl", ReturnUrl);
        map.put("OrderId", OrderId);
        map.put("Amount", Amount);
        map.put("PayType", PayType);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String Sign = SignatureUtils.md5(signContent + "&key=" + key).toUpperCase();
        map.put("Sign", Sign);
        map.put("Method", Method);
        map.put("MerchantId", MerchantId);

        String payGateway = "https://api.pppzf.cc/Gateway";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
