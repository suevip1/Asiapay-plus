package com.jeequan.jeepay.pay.channel.dealpay;

import cn.hutool.http.HttpResponse;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class DealpayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[deal支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.DEALPAY;
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

            String appId = normalMchParams.getMchNo();
            String openType = "open_url";
            String orderId = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String passCode = normalMchParams.getPayType();
            String mcPayName = "mcPayName";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String applyDate = dateFormat.format(new Date());
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String pageUrl = notifyUrl;


            map.put("appId", appId);
            map.put("openType", openType);
            map.put("orderId", orderId);
            map.put("amount", amount);
            map.put("notifyUrl", notifyUrl);
            map.put("passCode", passCode);
            map.put("mcPayName", mcPayName);
            map.put("applyDate", applyDate);
            map.put("pageUrl", pageUrl);


            String contentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""}) + key;
            String sign = SignatureUtils.md5(contentStr).toLowerCase();
            map.put("sign", sign);

            log.info("[{}]请求响应:{}", LOG_TAG, map);
            String payGateway = normalMchParams.getPayGateway();

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1")) {
                JSONObject data = result.getJSONObject("result");

                String payUrl = data.getString("returnUrl");
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
        String key = "60a36bb28d8adeec6088ba41981a8c2a";

        String appId = "24hx";
        String openType = "open_url";
        String orderId = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String passCode = "KM_WRM";
        String mcPayName = "mcPayName";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String applyDate = dateFormat.format(new Date());
        String notifyUrl = "https://www.test.com";
        String pageUrl = notifyUrl;


        map.put("appId", appId);
        map.put("openType", openType);
        map.put("orderId", orderId);
        map.put("amount", amount);
        map.put("notifyUrl", notifyUrl);
        map.put("passCode", passCode);
        map.put("mcPayName", mcPayName);
        map.put("applyDate", applyDate);
        map.put("pageUrl", pageUrl);


        String contentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""}) + key;
        String sign = SignatureUtils.md5(contentStr).toLowerCase();
        map.put("sign", sign);


        log.info("[{}]请求响应:{}", LOG_TAG, map);
        String payGateway = "http://8.218.223.244:29910/v2/deal/pay";

        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}