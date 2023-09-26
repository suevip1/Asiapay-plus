package com.jeequan.jeepay.pay.channel.shunxin;

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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * 顺鑫支付
 */
@Service
@Slf4j
public class ShunxinPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[顺鑫支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SHUNXIN;
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

            String merchantId = normalMchParams.getMchNo();
            String orderNo = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2Dollar(payOrder.getAmount());

            String timeSpan = String.valueOf(System.currentTimeMillis());
            String callBackUrl = getNotifyUrl(payOrder.getPayOrderId());

            String accountName = merchantId;
            String ip = payOrder.getClientIp();
            String productName = "下单";
            String productId = normalMchParams.getPayType();
            String returnUrl = callBackUrl;


            map.put("merchantId", merchantId);
            map.put("orderNo", orderNo);
            map.put("money", money);
            map.put("timeSpan", timeSpan);
            map.put("callBackUrl", callBackUrl);
            map.put("accountName", accountName);
            map.put("ip", ip);
            map.put("productName", productName);
            map.put("productId", productId);
            map.put("returnUrl", returnUrl);

            String sign = JeepayKit.getSign(map, key);
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("state").equals("0") && result.getString("payState").equals("2")) {
                String payUrl = result.getString("data");
                String passageOrderId = result.getString("platOrderNo");

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
        String key = "60EEFC14E8A8335613DA6EE34147252D";

        String merchantId = "eluosi";
        String orderNo = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2Dollar("3000");

        String timeSpan = String.valueOf(System.currentTimeMillis());
        String callBackUrl = "https://www.test.com";

        String accountName = "eluosi";
        String ip = "127.0.0.1";
        String productName = "下单";
        String productId = "1";
        String returnUrl = callBackUrl;


        map.put("merchantId", merchantId);
        map.put("orderNo", orderNo);
        map.put("money", money);
        map.put("timeSpan", timeSpan);
        map.put("callBackUrl", callBackUrl);
        map.put("accountName", accountName);
        map.put("ip", ip);
        map.put("productName", productName);
        map.put("productId", productId);
        map.put("returnUrl", returnUrl);

        String sign = JeepayKit.getSign(map, key);
        map.put("sign", sign);
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));


        String payGateway = "https://api.shunxing.xyz/api/commonpay/pay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}