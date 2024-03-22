package com.jeequan.jeepay.pay.channel.baituo;

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

@Service
@Slf4j
public class BaituoPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[拜托支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.BAITUO;
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

            Map<String, String> mapHead = new HashMap<>();
            Map<String, Object> map = new HashMap<>();
            String key = normalMchParams.getSecret();

            String appid = normalMchParams.getMchNo();
            String timestamp = System.currentTimeMillis() / 1000 + "";
            mapHead.put("appid", appid);
            mapHead.put("timestamp", timestamp);

            String pay_type = normalMchParams.getPayType();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String orderno = payOrder.getPayOrderId();
            String notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String clientip = payOrder.getClientIp();
            map.put("pay_type", pay_type);
            map.put("amount", amount);
            map.put("orderno", orderno);
            map.put("notifyurl", notifyurl);
            map.put("clientip", clientip);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String signContent2 = timestamp + key + signContent;
            String sign = SignatureUtils.md5(signContent2).toUpperCase();
            mapHead.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).headerMap(mapHead, false).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("pay_url");
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

        Map<String, String> mapHead = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        String key = "8308df94de1311ee865900163e01a9ee";

        String appid = "800237";
        String timestamp = System.currentTimeMillis() / 1000 + "";
        mapHead.put("appid", appid);
        mapHead.put("timestamp", timestamp);

        String pay_type = "132";
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String orderno = RandomStringUtils.random(15, true, true);
        String notifyurl = "http://www.test.com";
        String clientip = "127.0.0.1";
        map.put("pay_type", pay_type);
        map.put("amount", amount);
        map.put("orderno", orderno);
        map.put("notifyurl", notifyurl);
        map.put("clientip", clientip);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String signContent2 = timestamp + key + signContent;
        String sign = SignatureUtils.md5(signContent2).toUpperCase();
        mapHead.put("sign", sign);

        String payGateway = "http://gateway.baituo.shop/api/topay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).headerMap(mapHead, false).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
