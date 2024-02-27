package com.jeequan.jeepay.pay.channel.septpay;

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
public class SeptpayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[sept支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SEPTPAY;
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

            String merchantId = normalMchParams.getMchNo();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String gateway = normalMchParams.getPayType();
            String merOrderId = payOrder.getPayOrderId();
            Long amt = payOrder.getAmount();

            map.put("merchantId", merchantId);
            map.put("notifyUrl", notifyUrl);
            map.put("gateway", gateway);
            map.put("merOrderId", merOrderId);
            map.put("amt", amt);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
            String sign = SignatureUtils.md5(signStr);
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0000")) {

                String payUrl = result.getString("payUrl");
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
        String key = "cacd9a4ae76940b5b6e3f8ae95e260d7";

        String merchantId = "Y2400047811";
        String notifyUrl = "http://www.test.com";
        String gateway = "douyin";
        String merOrderId = RandomStringUtils.random(15, true, true);
        Long amt = 10000L;

        map.put("merchantId", merchantId);
        map.put("notifyUrl", notifyUrl);
        map.put("gateway", gateway);
        map.put("merOrderId", merOrderId);
        map.put("amt", amt);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
        String sign = SignatureUtils.md5(signStr);
        map.put("sign", sign);

        String payGateway = "http://13.212.246.176:8110/resolve/pay/v1";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
