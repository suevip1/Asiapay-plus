package com.jeequan.jeepay.pay.channel.goodpay;

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
public class GoodpayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "goodpay支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.GOODPAY;
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

            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;
            String order_id = payOrder.getPayOrderId();
            String order_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String pay_type = normalMchParams.getPayType();
            String user_id = normalMchParams.getMchNo();
            String sign_type = "MD5";
            String method = "topay";
            String client_ip = payOrder.getClientIp();
            String client_system = "ios";

            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            map.put("order_id", order_id);
            map.put("order_amount", order_amount);
            map.put("pay_type", pay_type);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);
            map.put("user_id", user_id);
            map.put("sign_type", sign_type);
            map.put("method", method);
            map.put("client_ip", client_ip);
            map.put("client_system", client_system);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map,10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("10000")) {

                String payUrl = result.getString("pay_url");
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
        String key = "67105915859C79974E6460E831BF42C4";

        String order_id = RandomStringUtils.random(15, true, true);
        String order_amount = AmountUtil.convertCent2Dollar("50000");
        String pay_type = "ddqhb";
        String notify_url = "https://www.test.com";
        String return_url = notify_url;
        String user_id = "300058";
        String sign_type = "MD5";
        String method = "topay";
        String client_ip = "127.0.0.1";
        String client_system = "ios";

        map.put("notify_url", notify_url);
        map.put("return_url", return_url);
        map.put("order_id", order_id);
        map.put("order_amount", order_amount);
        map.put("pay_type", pay_type);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);
        map.put("user_id", user_id);
        map.put("sign_type", sign_type);
        map.put("method", method);
        map.put("client_ip", client_ip);
        map.put("client_system", client_system);

        String payGateway = "https://api.goodpay123.com/api/gateway";

        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}