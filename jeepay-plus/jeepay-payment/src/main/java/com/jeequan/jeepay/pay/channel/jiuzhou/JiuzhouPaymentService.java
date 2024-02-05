package com.jeequan.jeepay.pay.channel.jiuzhou;

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
import java.util.Random;


@Service
@Slf4j
public class JiuzhouPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[九州支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.JIUZHOU;
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

            String customerNo = normalMchParams.getMchNo();
            String payTypeId = normalMchParams.getPayType();

            String orderNo = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String customerCallbackUrl = getNotifyUrl(payOrder.getPayOrderId());
            String timestamp = System.currentTimeMillis() + "";
            String ip = payOrder.getClientIp();
            String userId = SignatureUtils.md5(ip).toLowerCase();

            map.put("customerNo", customerNo);
            map.put("payTypeId", payTypeId);
            map.put("orderNo", orderNo);
            map.put("amount", amount);
            map.put("customerCallbackUrl", customerCallbackUrl);
            map.put("timestamp", timestamp);
            map.put("ip", ip);
            map.put("userId", userId);
            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);


            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            raw = HttpUtil.post(payGateway, map, 10000);

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("success").equals("true")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("url");
                String passageOrderId = data.getString("transactionalNumber");

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
        String key = "e39eb73dbd094014ba76258d8e3d0a9f";

        String customerNo = "sh1704961412373";
        String payTypeId = "25";

        String orderNo = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2DollarShort(10000L);
        String customerCallbackUrl = "http://www.test.com";
        String timestamp = System.currentTimeMillis() + "";
        String ip = "127.0.0.1";
        String userId = SignatureUtils.md5(ip).toLowerCase();

        map.put("customerNo", customerNo);
        map.put("payTypeId", payTypeId);
        map.put("orderNo", orderNo);
        map.put("amount", amount);
        map.put("customerCallbackUrl", customerCallbackUrl);
        map.put("timestamp", timestamp);
        map.put("ip", ip);
        map.put("userId", userId);
        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);


        String payGateway = "https://api.jiuzhouzf.net/c/payment/pay";

        // 发送POST请求并指定JSON数据
        raw = HttpUtil.post(payGateway, map, 10000);

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}