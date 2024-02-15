package com.jeequan.jeepay.pay.channel.storepay;

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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StorepayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[Store支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.STOREPAY;
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

            String store_id = normalMchParams.getMchNo();
            String order_sn = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2Dollar(payOrder.getPayOrderId());
            String pay_type = normalMchParams.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;

            map.put("store_id", store_id);
            map.put("order_sn", order_sn);
            map.put("money", money);
            map.put("pay_type", pay_type);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1")) {

                String payUrl = result.getString("url");
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
        String key = "416e23e8-bce7-4151-8629-0cf595018d64";

        String store_id = "10181";
        String order_sn = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2Dollar(3000L);
        String pay_type = "wxpay";
        String notify_url = "https://www.test.com";
        String return_url = notify_url;

        map.put("store_id", store_id);
        map.put("order_sn", order_sn);
        map.put("money", money);
        map.put("pay_type", pay_type);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);
        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://116.212.120.154:22044/index.php?s=/api/pay/index";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSONString(map)).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}