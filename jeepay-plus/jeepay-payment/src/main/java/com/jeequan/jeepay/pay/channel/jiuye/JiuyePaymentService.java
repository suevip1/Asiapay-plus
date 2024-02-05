package com.jeequan.jeepay.pay.channel.jiuye;

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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * 酒业支付
 */
@Service
@Slf4j
public class JiuyePaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[酒业支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.JIUYE;
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

            String merchant_id = normalMchParams.getMchNo();
            String order_sn = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String channel = normalMchParams.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            long time_stamp = System.currentTimeMillis();

            map.put("merchant_id", merchant_id);
            map.put("order_sn", order_sn);
            map.put("amount", amount);
            map.put("channel", channel);
            map.put("notify_url", notify_url);
            map.put("time_stamp", time_stamp);

            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
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

        Map<String, Object> map = new HashMap<>();
        String key = "rPWFItdkfrjaZDUbXbMNrmXFIxtHhVcM";

        String merchant_id = "10166";
        String order_sn = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String channel = "DigitalRMBPay";
        String notify_url = "http://www.test.com";
        long time_stamp = System.currentTimeMillis();

        map.put("merchant_id", merchant_id);
        map.put("order_sn", order_sn);
        map.put("amount", amount);
        map.put("channel", channel);
        map.put("notify_url", notify_url);
        map.put("time_stamp", time_stamp);

        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "https://api.hulizhifu.com/api/unifiedorder";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}