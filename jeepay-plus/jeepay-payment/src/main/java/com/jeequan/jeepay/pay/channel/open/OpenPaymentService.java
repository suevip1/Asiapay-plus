package com.jeequan.jeepay.pay.channel.open;

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
import com.jeequan.jeepay.pay.channel.xxpay7.Xxpay7ParamsModel;
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
public class OpenPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[Open支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.OPEN;
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

            String id = normalMchParams.getMchNo();
            String code = normalMchParams.getPayType();
            String out_trade_no = payOrder.getPayOrderId();
            String callback_url = getNotifyUrl(payOrder.getPayOrderId());
            String amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());

            map.put("id", id);
            map.put("code", code);
            map.put("out_trade_no", out_trade_no);
            map.put("amount", amount);
            map.put("callback_url", callback_url);


            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);

            String signValue = signStr + "&key=" + key;

            String sign = SignatureUtils.md5(signValue).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {

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
        String key = "rvt51d7eYLbiDD1nIlMua0ECeiu9UKXd";

        String id = "9Pe8VyXWjolZ6B1N7dOK";
        String code = "805";
        String out_trade_no = RandomStringUtils.random(15, true, true);
        String callback_url = "https://www.test.com";
        String amount = AmountUtil.convertCent2DollarShort(10000L);


        map.put("id", id);
        map.put("code", code);
        map.put("out_trade_no", out_trade_no);
        map.put("amount", amount);
        map.put("callback_url", callback_url);


        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);

        String signValue = signStr + "&key=" + key;

        String sign = SignatureUtils.md5(signValue).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://185.189.241.195/api/open/store";
        log.info("[{}]请求:{}", LOG_TAG, map);
        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}