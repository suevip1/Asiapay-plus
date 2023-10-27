package com.jeequan.jeepay.pay.channel.juding;

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

import java.util.HashMap;
import java.util.Map;

/**
 * 聚鼎支付
 */
@Service
@Slf4j
public class JudingPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[聚鼎支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.JUDING;
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

            String merchantUUID = normalMchParams.getMchNo();
            String price = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String payType = normalMchParams.getPayType();
            String merchantOrderNo = payOrder.getPayOrderId();
            String clientIp = payOrder.getClientIp();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = notifyUrl;

            map.put("merchantUUID", merchantUUID);
            map.put("price", price);
            map.put("payType", payType);
            map.put("merchantOrderNo", merchantOrderNo);
            map.put("clientIp", clientIp);
            map.put("notifyUrl", notifyUrl);
            map.put("returnUrl", returnUrl);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("sysOrderNo");

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
        String key = "rlfNW6LixpneKkJ0E4pcsQdRjKCHxl1y";

        String merchantUUID = "3dfd2704-a727-4955-8bbf-578e40b5e667";
        String price = AmountUtil.convertCent2Dollar(10000L);
        String payType = "101";
        String clientIp = "127.0.0.1";
        String merchantOrderNo = RandomStringUtils.random(15, true, true);
        String notifyUrl = "https://www.test.com";
        String returnUrl = notifyUrl;

        map.put("merchantUUID", merchantUUID);
        map.put("price", price);
        map.put("payType", payType);
        map.put("clientIp", clientIp);
        map.put("merchantOrderNo", merchantOrderNo);
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", returnUrl);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toUpperCase();
        map.put("sign", sign);

        String payGateway = "https://api.juding44fksjfldsqweqw.xyz/api/gateway/generateOrder";
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}