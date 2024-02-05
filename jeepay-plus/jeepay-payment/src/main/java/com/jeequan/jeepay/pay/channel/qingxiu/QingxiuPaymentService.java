package com.jeequan.jeepay.pay.channel.qingxiu;

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


@Service
@Slf4j
public class QingxiuPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[清秀支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.QINGXIU;
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

            String busId = normalMchParams.getMchNo();
            String orderNo = payOrder.getPayOrderId();
            String channelProductId = normalMchParams.getPayType();
            String orderAmount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String callbackUrl = notifyUrl;

            map.put("busId", busId);
            map.put("orderNo", orderNo);
            map.put("channelProductId", channelProductId);
            map.put("orderAmount", orderAmount);
            map.put("notifyUrl", notifyUrl);
            map.put("callbackUrl", callbackUrl);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);


            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            raw = HttpUtil.post(payGateway, map, 10000);

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                String payUrl = result.getString("data");
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
        String key = "bec5ceb720a74853b1413963ce46f2da";

        String busId = "1747081282027614209";
        String orderNo = RandomStringUtils.random(15, true, true);
        String channelProductId = "7777";
        String orderAmount = AmountUtil.convertCent2DollarShort(10000L);
        String notifyUrl = "http://www.test.com";
        String callbackUrl = notifyUrl;

        map.put("busId", busId);
        map.put("orderNo", orderNo);
        map.put("channelProductId", channelProductId);
        map.put("orderAmount", orderAmount);
        map.put("notifyUrl", notifyUrl);
        map.put("callbackUrl", callbackUrl);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);


        String payGateway = "https://qx.wps.com.co/manager/bus/order/create";

        // 发送POST请求并指定JSON数据
        raw = HttpUtil.post(payGateway, map, 10000);

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}