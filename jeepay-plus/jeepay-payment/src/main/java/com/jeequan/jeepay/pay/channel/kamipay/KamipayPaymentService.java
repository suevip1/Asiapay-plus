package com.jeequan.jeepay.pay.channel.kamipay;

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
import java.util.Random;


@Service
@Slf4j
public class KamipayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[卡密支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.KAMIPAY;
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

            String mchid = normalMchParams.getMchNo();
            String mch_order_id = payOrder.getPayOrderId();
            String price = AmountUtil.convertCent2Dollar(payOrder.getAmount());


            String card_type = normalMchParams.getPayType();

            String time = System.currentTimeMillis() / 1000 + "";
            String notify = getNotifyUrl(payOrder.getPayOrderId());
            String rand = RandomInt() + "";

            map.put("mchid", mchid);
            map.put("mch_order_id", mch_order_id);
            map.put("price", price);

            map.put("card_type", card_type);
            map.put("time", time);
            map.put("notify", notify);

            map.put("rand", rand);

            String signContent = mchid + mch_order_id + price + card_type + notify + time + rand + key;
            String sign = SignatureUtils.md5(signContent).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {

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
        String key = "91c1eab69ce4381dd7f60a1b67bf2766";

        String mchid = "9000023";
        String mch_order_id = RandomStringUtils.random(15, true, true);
        String price = AmountUtil.convertCent2Dollar(10000L);


        String card_type = "3";

        String time = System.currentTimeMillis() / 1000 + "";
        String notify = "https://www.test.com";
        String rand = RandomInt() + "";


        map.put("mchid", mchid);
        map.put("mch_order_id", mch_order_id);
        map.put("price", price);

        map.put("card_type", card_type);
        map.put("time", time);
        map.put("notify", notify);

        map.put("rand", rand);

        String signContent = mchid + mch_order_id + price + card_type + notify + time + rand + key;
        String sign = SignatureUtils.md5(signContent).toLowerCase();

        map.put("sign", sign);
        String payGateway = "http://162.216.240.188/api/pay";
        log.info("[{}]请求:{}", LOG_TAG, map);

        raw = HttpUtil.post(payGateway, map);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

    private static int RandomInt() {
        Random random = new Random();
        int lowerBound = 100000;
        int upperBound = 999999;
        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
    }
}