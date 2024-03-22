package com.jeequan.jeepay.pay.channel.fpay;

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

@Service
@Slf4j
public class DashiPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "大师支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.DASHI;
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

            String app_id = normalMchParams.getMchNo();
            String out_trade_no = payOrder.getPayOrderId();
            String subject = "subject";
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String channel = normalMchParams.getPayType();
            String client_ip = payOrder.getClientIp();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;

            map.put("app_id", app_id);
            map.put("out_trade_no", out_trade_no);
            map.put("subject", subject);
            map.put("amount", amount);
            map.put("channel", channel);
            map.put("client_ip", client_ip);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("return_code").equals("SUCCESS")) {
                String payUrl = result.getString("credential");
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
        String key = "7C8A71AB099B1BCABA2E936E6FC4E5F7";

        String app_id = "1683";
        String out_trade_no = RandomStringUtils.random(15, true, true);
        String subject = "subject";
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String channel = "604";
        String client_ip = "127.0.0.1";
        String notify_url = "https://www.test.com";
        String return_url = notify_url;

        map.put("app_id", app_id);
        map.put("out_trade_no", out_trade_no);
        map.put("subject", subject);
        map.put("amount", amount);
        map.put("channel", channel);
        map.put("client_ip", client_ip);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));

        String payGateway = "https://dashi.fpgw.net/apply";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}