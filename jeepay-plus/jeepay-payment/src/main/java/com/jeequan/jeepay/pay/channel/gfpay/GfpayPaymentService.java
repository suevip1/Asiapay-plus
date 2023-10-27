package com.jeequan.jeepay.pay.channel.gfpay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
 * Gfpay支付
 */
@Service
@Slf4j
public class GfpayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[Gfpay支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.GFPAY;
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

            String s_id = normalMchParams.getMchNo();
            String order_no = payOrder.getPayOrderId();
            String type = normalMchParams.getPayType();

            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());

            map.put("s_id", s_id);
            map.put("order_no", order_no);
            map.put("amount", amount);
            map.put("type", type);
            map.put("notify_url", notify_url);

            String signContent = amount + notify_url + order_no + s_id + type + key;
            String sign = SignatureUtils.md5(signContent).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.get(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("result").equals("SUCCESS")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("qrCode");

                res.setPayDataType(CS.PAY_DATA_TYPE.PAY_URL);
                res.setPayData(payUrl);

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
        String key = "0KWOlMptoR4dphxJSVEb8FImKPUmGsZNpHcY043lykpn2Aw5FWresqhsEDCfFUwD";

        String s_id = "16841608599742868785";
        String order_no = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(1000L);
        String type = "2";
        String notify_url = "https://www.test.com";

        map.put("s_id", s_id);
        map.put("order_no", order_no);
        map.put("amount", amount);
        map.put("type", type);
        map.put("notify_url", notify_url);

        String signContent = amount + notify_url + order_no + s_id + type + key;
        String sign = SignatureUtils.md5(signContent).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://47.106.254.76:8090/api/order/create";
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));

        raw = HttpUtil.get(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}