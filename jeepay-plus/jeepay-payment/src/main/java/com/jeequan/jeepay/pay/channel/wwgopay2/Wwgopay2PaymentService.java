package com.jeequan.jeepay.pay.channel.wwgopay2;

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
public class Wwgopay2PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[wwgopay支付2]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WWGOPAY2;
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

            String api_id = normalMchParams.getMchNo();
            String orderid = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2DollarShort(payOrder.getAmount().toString());
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;
            String ip = payOrder.getClientIp();
            String type = normalMchParams.getPayType();

            map.put("api_id", api_id);
            map.put("orderid", orderid);
            map.put("money", money);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);
            map.put("ip", ip);
            map.put("type", type);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map,10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {

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
        String key = "553DA877EE1CA970DD015320F7A8BDE3";

        String api_id = "1894098";
        String orderid = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2DollarShort("10000");
        String notify_url = "https://www.test.com";
        String return_url = notify_url;
        String ip = "127.0.0.1";
        String type = "wxpay";

        map.put("api_id", api_id);
        map.put("orderid", orderid);
        map.put("money", money);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);
        map.put("ip", ip);
        map.put("type", type);

        String payGateway = "https://pay.hzpay.cc/api/pay";
        log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));
        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}