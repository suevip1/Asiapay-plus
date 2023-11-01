package com.jeequan.jeepay.pay.channel.xiami;

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
 * 虾米支付
 */
@Service
@Slf4j
public class XiamiPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[虾米支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XIAMI;
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

            String mchId = normalMchParams.getMchNo();
            String mchOrderNo = payOrder.getPayOrderId();
            String productId = normalMchParams.getPayType();
            long orderAmount = payOrder.getAmount();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String clientIp = payOrder.getClientIp();
            String device = "ios";
            String extra = "extra";

            map.put("mchId", mchId);
            map.put("mchOrderNo", mchOrderNo);
            map.put("productId", productId);
            map.put("orderAmount", orderAmount);
            map.put("notifyUrl", notifyUrl);
            map.put("clientIp", clientIp);
            map.put("device", device);
            map.put("extra", extra);

            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("payOrderId");

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
        String key = "5dc01376359e425baa5d72a98ffd06d7";

        String mchId = "1053";
        String mchOrderNo = RandomStringUtils.random(15, true, true);
        String productId = "15";
        long orderAmount = 10000;
        String notifyUrl = "https://www.test.com";
        String clientIp = "127.0.0.1";
        String device = "ios";
        String extra = "extra";


        map.put("mchId", mchId);
        map.put("mchOrderNo", mchOrderNo);
        map.put("productId", productId);
        map.put("orderAmount", orderAmount);
        map.put("notifyUrl", notifyUrl);
        map.put("clientIp", clientIp);
        map.put("device", device);
        map.put("extra", extra);

        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://150.109.73.69/api/pay/create_order";
        log.info("[{}]请求Map:{}", LOG_TAG, map);

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}