package com.jeequan.jeepay.pay.channel.cyoupay;

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
public class CyoupayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[CYOU支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.CYOUPAY;
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
            CyoupayParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), CyoupayParams.class);

            Map<String, Object> map = new HashMap<>();
            String secret = normalMchParams.getSecret();

            String key = normalMchParams.getKey();
            String merchantKey = normalMchParams.getMerchantKey();

            String code = normalMchParams.getCode();
            String payType = normalMchParams.getPayType();

            String sfOrderId = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2DollarShort(payOrder.getAmount());

            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            // payType:"0" ,  //0.支付宝 1.微信 2.qq钱包 3.云闪付
            map.put("key", key);
            map.put("merchantKey", merchantKey);
            map.put("code", code);
            map.put("sfOrderId", sfOrderId);
            map.put("payType", payType);

            map.put("money", money);
            map.put("notifyUrl", notifyUrl);

            String signStr = "sign=" + secret + "&payType=" + payType + "&code=" + code + "&money=" + money + "&sfOrderId=" + sfOrderId + "&notifyUrl=" + notifyUrl;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {

                String payUrl = result.getString("payUrl");
                String passageOrderId = result.getString("orderId");

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
        String secret = "zLDVbutkkukLlAx8";


        String key = "Xrn2c5I0igKWsidJSXGIlU";
        String merchantKey = "a2R3hvnWsIfeGoYZRHCUQ5";

        String code = "1014";
        String payType = "0";

        String sfOrderId = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2DollarShort(10000L);

        String notifyUrl = "https://www.test.com";

        // payType:"0" ,  //0.支付宝 1.微信 2.qq钱包 3.云闪付
        map.put("key", key);
        map.put("merchantKey", merchantKey);
        map.put("code", code);
        map.put("sfOrderId", sfOrderId);
        map.put("payType", payType);

        map.put("money", money);
        map.put("notifyUrl", notifyUrl);

        String signStr = "sign=" + secret + "&payType=" + payType + "&code=" + code + "&money=" + money + "&sfOrderId=" + sfOrderId + "&notifyUrl=" + notifyUrl;
        log.info("签名串:" + signStr);
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://47.101.209.70/api/sfSendOrder";
        log.info("请求体:" + JSONObject.toJSONString(map));
        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}