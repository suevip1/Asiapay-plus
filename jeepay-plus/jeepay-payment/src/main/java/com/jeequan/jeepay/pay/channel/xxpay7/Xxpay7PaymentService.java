package com.jeequan.jeepay.pay.channel.xxpay7;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class Xxpay7PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[xxpay7支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XXPAY7;
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
            Xxpay7ParamsModel normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), Xxpay7ParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = normalMchParams.getSecret();

            String mchNo = normalMchParams.getMchNo();
            String appId = normalMchParams.getAppId();
            String wayCode = normalMchParams.getPayType();
            String mchOrderNo = payOrder.getPayOrderId();
            String amount = payOrder.getAmount() + "";
            String currency = "cny";

            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            String subject = "subject";
            String body = "body";

            String reqTime = System.currentTimeMillis() + "";
            String version = "1.0";
            String signType = "MD5";

            map.put("mchNo", mchNo);
            map.put("appId", appId);
            map.put("wayCode", wayCode);
            map.put("amount", amount);
            map.put("mchOrderNo", mchOrderNo);

            map.put("currency", currency);
            map.put("notifyUrl", notifyUrl);
            map.put("subject", subject);
            map.put("body", body);

            map.put("reqTime", reqTime);
            map.put("version", version);
            map.put("signType", signType);
            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);

            String signValue = signStr + "&key=" + key;

            String sign = SignatureUtils.md5(signValue).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");


                String payUrl = data.getString("payData");
                String passageOrderId = data.getString("payOrderId");

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
        String key = "a7Tf2oaDXCpjloIqtpnPqXbtbz8kK2aFOIR8Piml2LzStLkFy8OnXsecYiPPkQLqYvgfSjBwzalP5us8Fy8OE3ekGr8xQSAk7DZbdB0KcILnPRRjniaT7vdy7QwCNWrA";

        String mchNo = "M1703163003";
        String appId = "65851b7ceaf6d149c19dffdc";
        String wayCode = "ALI_QR";
        String mchOrderNo = RandomStringUtils.random(15, true, true);
        String amount = "10000";
        String currency = "cny";

        String notifyUrl = "https://www.test.com";

        String subject = "subject";
        String body = "body";

        String reqTime = System.currentTimeMillis() + "";
        String version = "1.0";
        String signType = "MD5";

        map.put("mchNo", mchNo);
        map.put("appId", appId);
        map.put("wayCode", wayCode);
        map.put("amount", amount);
        map.put("mchOrderNo", mchOrderNo);

        map.put("currency", currency);
        map.put("notifyUrl", notifyUrl);
        map.put("subject", subject);
        map.put("body", body);

        map.put("reqTime", reqTime);
        map.put("version", version);
        map.put("signType", signType);
        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);

        String signValue = signStr + "&key=" + key;

        String sign = SignatureUtils.md5(signValue).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://pay.aold.top/api/pay/unifiedOrder";
        log.info("[{}]请求:{}", LOG_TAG, map);
        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}