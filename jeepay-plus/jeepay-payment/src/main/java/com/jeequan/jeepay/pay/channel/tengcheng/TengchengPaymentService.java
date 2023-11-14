package com.jeequan.jeepay.pay.channel.tengcheng;

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


/**
 * 腾澄支付
 */
@Service
@Slf4j
public class TengchengPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[腾澄支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.TENGCHENG;
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

            String mid = normalMchParams.getMchNo();
            String merOrderTid = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String channelCode = normalMchParams.getPayType();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("mid", mid);
            map.put("merOrderTid", merOrderTid);
            map.put("money", money);
            map.put("channelCode", channelCode);
            map.put("notifyUrl", notifyUrl);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&" + key;
            String sign = SignatureUtils.md5(signStr).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("status").equals("0")) {
                JSONObject data = result.getJSONObject("result");

                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("tid");

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
        String key = "dc6f0b1fa2e301dce2c9a9aefd19313d";

        String mid = "M200011";
        String merOrderTid = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2Dollar(10000L);
        String channelCode = "166";
        String notifyUrl = "https://www.test.com";

        map.put("mid", mid);
        map.put("merOrderTid", merOrderTid);
        map.put("money", money);
        map.put("channelCode", channelCode);
        map.put("notifyUrl", notifyUrl);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&" + key;
        log.info("[{}]加密signStr:{}", LOG_TAG, signStr);
        String sign = SignatureUtils.md5(signStr).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://sfapi.itpay.vip/api/services/app/Api_PayOrder/CreateOrderPay";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}