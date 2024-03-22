package com.jeequan.jeepay.pay.channel.moquepay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
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

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class MoquepayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[MQ支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.MOQUEPAY;
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

            String customerId = normalMchParams.getMchNo();
            String orderId = payOrder.getPayOrderId();
            String batchno = orderId;
            String productCode = normalMchParams.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String timestamp = System.currentTimeMillis() / 1000 + "";
            String amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());

            map.put("customerId", customerId);
            map.put("orderId", orderId);
            map.put("batchno", batchno);
            map.put("productCode", productCode);
            map.put("notify_url", notify_url);
            map.put("timestamp", timestamp);
            map.put("amount", amount);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1")) {

                String payUrl = result.getJSONObject("data").getString("url");
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
        String key = "m2C8fYspaNDmHUlFt0O3fbYb9sB4vb";

        String customerId = "7";
        String orderId = RandomStringUtils.random(15, true, true);
        String batchno = orderId;
        String productCode = "6603";
        String notify_url = "https://www.test.com";
        String timestamp = System.currentTimeMillis() / 1000 + "";
        String amount = AmountUtil.convertCent2DollarShort(50000L);

        map.put("customerId", customerId);
        map.put("orderId", orderId);
        map.put("batchno", batchno);
        map.put("productCode", productCode);
        map.put("notify_url", notify_url);
        map.put("timestamp", timestamp);
        map.put("amount", amount);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);


        String payGateway = "http://api.moque0791.cn/submitpay.html";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);

        JSONObject result = JSON.parseObject(raw, JSONObject.class);
        //拉起订单成功
        if (result.getString("code").equals("200")) {
            String payUrl = "";
            String data = result.getString("data");
            if (StringUtils.isNotEmpty(data)) {
                payUrl = data;
            }
            String dataPayUrl = result.getString("payurl");
            if (StringUtils.isNotEmpty(dataPayUrl)) {
                payUrl = dataPayUrl;
            }
            String passageOrderId = "";
            log.info("[{}]请求响应:{}", LOG_TAG, payUrl);
        }
    }
}