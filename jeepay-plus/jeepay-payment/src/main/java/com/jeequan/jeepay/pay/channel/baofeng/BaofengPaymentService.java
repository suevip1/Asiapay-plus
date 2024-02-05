package com.jeequan.jeepay.pay.channel.baofeng;

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
 * 暴风支付
 */
@Service
@Slf4j
public class BaofengPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[BF支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.BAOFENG;
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

            String orderNo = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = notifyUrl;
            String userId = normalMchParams.getMchNo();
            String channel = normalMchParams.getPayType();

            map.put("orderNo", orderNo);
            map.put("amount", amount);
            map.put("notifyUrl", notifyUrl);
            map.put("returnUrl", returnUrl);
            map.put("userId", userId);
            map.put("channel", channel);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String sign = SignatureUtils.md5(signContent + key).toLowerCase();
            map.put("sign", sign);
            map.put("jsonType", "1");

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("status").equals("1")) {

                String payUrl = result.getJSONObject("data").getString("payUrl");
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
        String key = "ZW9VB42Q2S8VFRZUMSN6YLXVH1X6X1042HATIAG5YG6ASO0SNYWFW8K8BS9MSST2VAC6FR7MHW35TXZ1IREFKKOJ0U7SQ00SVV66TR7ENATUM5MIOEA3KPQJSDLMW92H";

        String orderNo = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String notifyUrl = "https://www.test.com";
        String returnUrl = notifyUrl;
        String userId = "1380";
        String channel = "222";

        map.put("orderNo", orderNo);
        map.put("amount", amount);
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", returnUrl);
        map.put("userId", userId);
        map.put("channel", channel);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String sign = SignatureUtils.md5(signContent + key).toLowerCase();
        map.put("sign", sign);
        map.put("jsonType", "1");

        String payGateway = "http://154.19.247.5:37577/pay/defray/singleOrder";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}