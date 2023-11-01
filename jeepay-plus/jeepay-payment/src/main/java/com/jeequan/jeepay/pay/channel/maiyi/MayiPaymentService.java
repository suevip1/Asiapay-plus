package com.jeequan.jeepay.pay.channel.maiyi;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
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

/**
 * 蚂蚁支付
 */
@Service
@Slf4j
public class MayiPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[蚂蚁支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.MAYI;
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
        try {
            PayPassage payPassage = payConfigContext.getPayPassage();
            //支付参数转换
            MayiParamsModel mayiParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), MayiParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = mayiParamsModel.getSecret();

            String merchantId = mayiParamsModel.getMchNo();
            String merchantOrderNo = payOrder.getPayOrderId();
            String channelCode = mayiParamsModel.getChannelCode();
            String payMoney = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String payType = mayiParamsModel.getPayType();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            long ts = System.currentTimeMillis();

            map.put("merchantId", merchantId);
            map.put("merchantOrderNo", merchantOrderNo);
            map.put("channelCode", channelCode);
            map.put("payMoney", payMoney);
            map.put("payType", payType);
            map.put("notifyUrl", notifyUrl);

            String signContent = SignatureUtils.getSignContent(map);
            String sign = SignatureUtils.md5(signContent + key).toLowerCase();
            map.put("sign", sign);
            map.put("ts", ts);

            String payGateway = mayiParamsModel.getPayGateway();

            String contentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""});
            log.info("[{}]请求参数:{}", LOG_TAG, contentStr);

            //请求支付页面
            String payUrl = payGateway + "/#/wait?" + contentStr;

            res.setPayDataType(CS.PAY_DATA_TYPE.PAY_URL);
            res.setPayData(payUrl);

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
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
        String key = "OzvxrQb8713852EAErEUWlvhx062894006";

        String merchantId = "10";
        String merchantOrderNo = RandomStringUtils.random(15, true, true);
        String channelCode = "9018";
        String payMoney = AmountUtil.convertCent2Dollar(5000L);
        String payType = "zhifubao";
        String notifyUrl = "https://www.test.com";
        long ts = System.currentTimeMillis();

        map.put("merchantId", merchantId);
        map.put("merchantOrderNo", merchantOrderNo);
        map.put("channelCode", channelCode);
        map.put("payMoney", payMoney);
        map.put("payType", payType);
        map.put("notifyUrl", notifyUrl);

        String signContent = SignatureUtils.getSignContent(map);
        String sign = SignatureUtils.md5(signContent + key).toLowerCase();
        map.put("sign", sign);
        map.put("ts", ts);

        String contentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""});
        log.info("[{}]请求参数:{}", LOG_TAG, contentStr);

        String payGateway = "http://8.217.215.251";
        log.info("[{}]请求:{}", LOG_TAG, payGateway + "/#/wait?" + contentStr);
    }
}