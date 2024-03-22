package com.jeequan.jeepay.pay.channel.jike;

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
public class JikePaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[即刻支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.JIKE;
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

            String Amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String ClientRealName = "name";
            String Ip = payOrder.getClientIp();
            String MerchantId = normalMchParams.getMchNo();
            String MerchantUniqueOrderId = payOrder.getPayOrderId();
            String NotifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String PayTypeId = normalMchParams.getPayType();
            String PayTypeIdFormat = "URL";
            String Remark = "";

            map.put("Amount", Amount);
            map.put("ClientRealName", ClientRealName);
            map.put("Ip", Ip);
            map.put("MerchantId", MerchantId);
            map.put("MerchantUniqueOrderId", MerchantUniqueOrderId);
            map.put("NotifyUrl", NotifyUrl);
            map.put("PayTypeId", PayTypeId);
            map.put("PayTypeIdFormat", PayTypeIdFormat);
            map.put("Remark", Remark);

            String signStr = SignatureUtils.getSignContent(map, null, null) + key;
            String Sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("Sign", Sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("Code").equals("0")) {

                String payUrl = result.getString("Url");
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
        String key = "km1AW6z2ear27u9esQUmRM4V0y1ThgNuyBq5dtZ4OmE8";

        String Amount = AmountUtil.convertCent2Dollar(10000L);
        String ClientRealName = "name";
        String Ip = "127.0.0.1";
        String MerchantId = "66969";
        String MerchantUniqueOrderId = RandomStringUtils.random(15, true, true);
        String NotifyUrl = "https://www.test.com";
        String PayTypeId = "jdecard";
        String PayTypeIdFormat = "URL";
        String Remark = "";

        map.put("Amount", Amount);
        map.put("ClientRealName", ClientRealName);
        map.put("Ip", Ip);
        map.put("MerchantId", MerchantId);
        map.put("MerchantUniqueOrderId", MerchantUniqueOrderId);
        map.put("NotifyUrl", NotifyUrl);
        map.put("PayTypeId", PayTypeId);
        map.put("PayTypeIdFormat", PayTypeIdFormat);
        map.put("Remark", Remark);

        String signStr = SignatureUtils.getSignContent(map, null, null)  + key;
        String Sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("Sign", Sign);

        String payGateway = "https://apib6kw57w.fghfu.xyz/InterfaceV9/CreatePayOrder/";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}