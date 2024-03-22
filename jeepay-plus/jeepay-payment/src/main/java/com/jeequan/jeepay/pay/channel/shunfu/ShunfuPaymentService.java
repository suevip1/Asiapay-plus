package com.jeequan.jeepay.pay.channel.shunfu;

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
public class ShunfuPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[顺付支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SHUNFU;
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

            String Pay_MerID = normalMchParams.getMchNo();
            String Pay_Type = normalMchParams.getPayType();
            String Pay_Amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String Pay_Ord = payOrder.getPayOrderId();
            String Pay_Notify = getNotifyUrl(payOrder.getPayOrderId());
            String Pay_Ip = payOrder.getClientIp();

            map.put("Pay_MerID", Pay_MerID);
            map.put("Pay_Type", Pay_Type);
            map.put("Pay_Amount", Pay_Amount);
            map.put("Pay_Ord", Pay_Ord);
            map.put("Pay_Notify", Pay_Notify);

            String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);
            map.put("Pay_Ip", Pay_Ip);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0000")) {

                String payUrl = result.getString("payurl");
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
        String key = "ed10cf235a314a7d9f7bf4e70792d3fa";

        String Pay_MerID = "3897";
        String Pay_Type = "60013";
        String Pay_Amount = AmountUtil.convertCent2Dollar(10000L);
        String Pay_Ord = RandomStringUtils.random(15, true, true);
        String Pay_Notify = "http://www.test.com";
        String Pay_Ip = "127.0.0.1";

        map.put("Pay_MerID", Pay_MerID);
        map.put("Pay_Type", Pay_Type);
        map.put("Pay_Amount", Pay_Amount);
        map.put("Pay_Ord", Pay_Ord);
        map.put("Pay_Notify", Pay_Notify);

        String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);
        map.put("Pay_Ip", Pay_Ip);

        String payGateway = "http://112.213.98.142:8099//api/recharge.aspx";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
