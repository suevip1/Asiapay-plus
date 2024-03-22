package com.jeequan.jeepay.pay.channel.zhifupay;

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
public class ZhifupayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[Zhifupay支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ZHIFUPAY;
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

            String merchant_no = normalMchParams.getMchNo();
            String pay_type = normalMchParams.getPayType();
            String out_order_no = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());


            map.put("merchant_no", merchant_no);
            map.put("pay_type", pay_type);
            map.put("out_order_no", out_order_no);
            map.put("amount", amount);
            map.put("notify_url", notify_url);


            String signStr = merchant_no + out_order_no + amount + pay_type + notify_url + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

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
        String key = "330dddeb4830ef0b5b2b66cba43cc1ab";

        String merchant_no = "10004";
        String pay_type = "alipay";
        String out_order_no = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2DollarShort(10000L);
        String notify_url = "http://www.test.com";


        map.put("merchant_no", merchant_no);
        map.put("pay_type", pay_type);
        map.put("out_order_no", out_order_no);
        map.put("amount", amount);
        map.put("notify_url", notify_url);


        String signStr = merchant_no + out_order_no + amount + pay_type + notify_url + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);


        String payGateway = "http://8.217.173.142:16237/api/pay";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求:{}", LOG_TAG, JSONObject.toJSON(map).toString());

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}