package com.jeequan.jeepay.pay.channel.galipay2;

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
public class Galipay2PaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[Galipay支付2]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.GALIPAY2;
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

            String merId = normalMchParams.getMchNo();
            String version = "2.0.1";
            String reqTime = System.currentTimeMillis() / 1000 + "";
            Long amount = payOrder.getAmount();
            String merOrderId = payOrder.getPayOrderId();
            String payType = normalMchParams.getPayType();
            String title = "pay";
            String synUrl = getNotifyUrl(payOrder.getPayOrderId());
            String selfParam = "extParam";
            String frontUrl = synUrl;

            map.put("merId", merId);
            map.put("version", version);
            map.put("reqTime", reqTime);
            map.put("amount", amount);
            map.put("merOrderId", merOrderId);
            map.put("payType", payType);
            map.put("title", title);
            map.put("synUrl", synUrl);
            map.put("selfParam", selfParam);
            map.put("frontUrl", frontUrl);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("responseCode").equals("0") && result.getString("resultMsg").equals("success")) {

                String payUrl = result.getString("payPath");
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
        String key = "YTBlOTFkNTZkM2MxNGFiNDgxNDcwZGM1YTFhMmQxY2JDQkRD";

        String merId = "20240118160038958";
        String version = "2.0.1";
        String reqTime = System.currentTimeMillis() / 1000 + "";
        Long amount = 10000L;
        String merOrderId = RandomStringUtils.random(15, true, true);
        String payType = "1033";
        String title = "pay";
        String synUrl = "http://www.test.com";
        String selfParam = "extParam";
        String frontUrl = synUrl;

        map.put("merId", merId);
        map.put("version", version);
        map.put("reqTime", reqTime);
        map.put("amount", amount);
        map.put("merOrderId", merOrderId);
        map.put("payType", payType);
        map.put("title", title);
        map.put("synUrl", synUrl);
        map.put("selfParam", selfParam);
        map.put("frontUrl", frontUrl);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://italy.sjrczd.com/mwap/gateway/wellpay";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}