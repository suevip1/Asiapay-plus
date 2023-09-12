package com.jeequan.jeepay.pay.channel.changsheng;

import cn.hutool.http.HttpResponse;
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
public class ChangshengPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[昌盛支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.CHANGSHENG;
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

            String AccessKey = normalMchParams.getMchNo();

            String PayChannelId = normalMchParams.getPayType();
            String OrderNo = payOrder.getPayOrderId();
            String Amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());

            String CallbackUrl = getNotifyUrl(payOrder.getPayOrderId());
            Long Timestamp = System.currentTimeMillis() / 1000;

            map.put("Timestamp", Timestamp);
            map.put("AccessKey", AccessKey);
            map.put("PayChannelId", PayChannelId);
            map.put("OrderNo", OrderNo);

            map.put("Amount", Amount);
            map.put("CallbackUrl", CallbackUrl);

            String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&SecretKey=" + key;
            String Sign = SignatureUtils.md5(SignStr).toLowerCase();
            map.put("Sign", Sign);

            String payGateway = normalMchParams.getPayGateway();

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSONString(map)).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("Code").equals("0")) {
                JSONObject data = result.getJSONObject("Data");
                String payUrl = data.getJSONObject("PayeeInfo").getString("CashUrl");
                String passageOrderId = data.getString("OrderNo");

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
        String key = "rJVeoBam32Svmw53rk6oIn4YZqgBR4s6VMNeb97R";

        Long Timestamp = System.currentTimeMillis() / 1000;
        String AccessKey = "b9rk4YGOYmIrMg3Qy2x7cynVBAz";

        String PayChannelId = "1517";
        String OrderNo = RandomStringUtils.random(15, true, true);
        String Amount = AmountUtil.convertCent2Dollar(200000L);

        String CallbackUrl = "https://www.test.com";


        map.put("Timestamp", Timestamp);
        map.put("AccessKey", AccessKey);
        map.put("PayChannelId", PayChannelId);
        map.put("OrderNo", OrderNo);

        map.put("Amount", Amount);
        map.put("CallbackUrl", CallbackUrl);

        String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&SecretKey=" + key;
        String Sign = SignatureUtils.md5(SignStr).toLowerCase();
        map.put("Sign", Sign);

        String payGateway = "https://merchant.tengdazhifu.xyz/api/PayV2/submit";
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json") // 指定请求体的Content-Type为JSON
                .execute();
        raw = response.body();

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
        JSONObject result = JSON.parseObject(raw, JSONObject.class);
        log.info("[{}]请求响应:{}", LOG_TAG, result.toJSONString());
    }
}