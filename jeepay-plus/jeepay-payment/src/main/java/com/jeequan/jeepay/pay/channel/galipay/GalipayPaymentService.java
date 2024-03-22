package com.jeequan.jeepay.pay.channel.galipay;

import cn.hutool.http.HttpResponse;
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
import java.util.TreeMap;


@Service
@Slf4j
public class GalipayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[Galipay支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.GALIPAY;
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

            String key = normalMchParams.getSecret();

            Map<String, String> mapHead = new HashMap<>();

            String merId = normalMchParams.getMchNo();
            String version = "1.0.1";
            String osName = "1001";
            String reqTime = System.currentTimeMillis() / 1000 + "";

            mapHead.put("merId", merId);
            mapHead.put("version", version);
            mapHead.put("osName", osName);
            mapHead.put("reqTime", reqTime);

            Map<String, Object> mapData = new HashMap<>();
            Long amount = payOrder.getAmount();
            String merOrderId = payOrder.getPayOrderId();

            String payType = normalMchParams.getPayType();
            String title = "pay";
            String selfParam = "extParam";
            String synUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnurl = synUrl;

            mapData.put("amount", amount);
            mapData.put("merOrderId", merOrderId);
            mapData.put("payType", payType);
            mapData.put("returnurl", returnurl);
            mapData.put("selfParam", selfParam);
            mapData.put("synUrl", synUrl);
            mapData.put("title", title);

            Map<String, Object> sortedMap = new TreeMap<>(mapData);
            String signStr = JSONObject.toJSONString(sortedMap) + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            mapHead.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(sortedMap));

            HttpResponse response = HttpUtil.createPost(payGateway).body("data="+JSONObject.toJSONString(sortedMap)).headerMap(mapHead, false).contentType("application/x-www-form-urlencoded").timeout(10000)
                    .execute();
            // 处理响应
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("resultCode").equals("1001")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("payPath");
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


        String key = "YTBlOTFkNTZkM2MxNGFiNDgxNDcwZGM1YTFhMmQxY2JDQkRD";

        Map<String, String> mapHead = new HashMap<>();

        String merId = "20240118160038958";
        String version = "1.0.1";
        String osName = "1001";
        String reqTime = System.currentTimeMillis() / 1000 + "";

        mapHead.put("merId", merId);
        mapHead.put("version", version);
        mapHead.put("osName", osName);
        mapHead.put("reqTime", reqTime);

        Map<String, Object> mapData = new HashMap<>();
        Long amount = 10000L;
        String merOrderId = RandomStringUtils.random(15, true, true);

        String payType = "1033";
        String title = "pay";
        String selfParam = "extParam";
        String synUrl = "http://www.test.com";
        String returnurl = synUrl;

        mapData.put("amount", amount);
        mapData.put("merOrderId", merOrderId);
        mapData.put("payType", payType);
        mapData.put("returnurl", returnurl);
        mapData.put("selfParam", selfParam);
        mapData.put("synUrl", synUrl);
        mapData.put("title", title);

        Map<String, Object> sortedMap = new TreeMap<>(mapData);
        String signStr = JSONObject.toJSONString(sortedMap) + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();

        mapHead.put("sign", sign);

        String payGateway = "http://italy.sjrczd.com/mwap/api/galipay";


        HttpResponse response = HttpUtil.createPost(payGateway).body("data="+JSONObject.toJSONString(sortedMap)).headerMap(mapHead, false).contentType("application/x-www-form-urlencoded").timeout(10000)
                .execute();
        // 处理响应
        raw = response.body();

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}