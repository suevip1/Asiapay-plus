package com.jeequan.jeepay.pay.channel.xxpay2;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
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
 * xxpay2支付
 */
@Service
@Slf4j
public class Xxpay2PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[xxpay2支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XXPAY2;
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

            String mchId = normalMchParams.getMchNo();
            String mchOrderNo = payOrder.getPayOrderId();
            String channelId = normalMchParams.getPayType();

            long amount = payOrder.getAmount();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            String subject = "subject1";
            String body = "body1";

            String clientIp = payOrder.getClientIp();

            map.put("mchId", mchId);
            map.put("channelId", channelId);
            map.put("amount", amount);
            map.put("mchOrderNo", mchOrderNo);

            map.put("clientIp", clientIp);
            map.put("subject", subject);
            map.put("body", body);

            map.put("notifyUrl", notifyUrl);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("status").equals("200")) {

                String payUrl = result.getString("payUrl");
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
        String key = "7dcb99a99452ae16c338b516a94030b1";

        String mchId = "1100072";
        String mchOrderNo = RandomStringUtils.random(15, true, true);
        String channelId = "8002";

        long amount = 10000;
        String notifyUrl = "https://www.test.com";


        String subject = "subject";
        String body = "body";

        String clientIp = "127.0.0.1";

        map.put("mchId", mchId);
        map.put("channelId", channelId);
        map.put("amount", amount);
        map.put("mchOrderNo", mchOrderNo);

        map.put("clientIp", clientIp);
        map.put("subject", subject);
        map.put("body", body);

        map.put("notifyUrl", notifyUrl);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://103.13.230.213:2088/api/pay/create_order";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
        JSONObject result = JSON.parseObject(raw, JSONObject.class);
        log.info("[{}]请求响应:{}", LOG_TAG, result.toJSONString());
    }
}
