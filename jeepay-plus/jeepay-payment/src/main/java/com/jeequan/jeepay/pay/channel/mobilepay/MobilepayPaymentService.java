package com.jeequan.jeepay.pay.channel.mobilepay;

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
public class MobilepayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[Mobile支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.MOBILEPAY;
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

            String customerNumber = normalMchParams.getMchNo();
            String payType = normalMchParams.getPayType();
            String orderNumber = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String callBackUrl = getNotifyUrl(payOrder.getPayOrderId());
            String playUserIp = payOrder.getClientIp();

            map.put("customerNumber", customerNumber);
            map.put("payType", payType);
            map.put("orderNumber", orderNumber);
            map.put("amount", amount);
            map.put("callBackUrl", callBackUrl);
            map.put("playUserIp", playUserIp);


            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toUpperCase();
            map.put("sign", sign);


            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("10000")) {

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
        String key = "f7d461f547964c9eb639730cfbf523c6";

        String customerNumber = "102252";
        String payType = "27";
        String orderNumber = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String callBackUrl = "http://www.test.com";
        String playUserIp = "127.0.0.1";

        map.put("customerNumber", customerNumber);
        map.put("payType", payType);
        map.put("orderNumber", orderNumber);
        map.put("amount", amount);
        map.put("callBackUrl", callBackUrl);
        map.put("playUserIp", playUserIp);


        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toUpperCase();
        map.put("sign", sign);


        String payGateway = "http://43.198.97.242:29110/mobile/order/created";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求:{}", LOG_TAG, JSONObject.toJSON(map).toString());

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}