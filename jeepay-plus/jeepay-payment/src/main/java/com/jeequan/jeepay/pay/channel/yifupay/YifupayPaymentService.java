package com.jeequan.jeepay.pay.channel.yifupay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 卡密支付
 */
@Service
@Slf4j
public class YifupayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "亿付支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YIFUPAY;
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

            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("app_id", normalMchParams.getMchNo());

            BigDecimal bigDecimal = new BigDecimal(String.valueOf(payOrder.getAmount().longValue() / 100L));
            DecimalFormat decimalFormat = new DecimalFormat("0.00#");
            String strVal = decimalFormat.format(bigDecimal);

            map.put("amount", strVal);
            map.put("out_trade_id", payOrder.getPayOrderId());
            map.put("payment_code", normalMchParams.getPayType());
            map.put("notify_url", notifyUrl);
            map.put("callback_url", notifyUrl);

            String signContent = SignatureUtils.getSignContent(map, null, null) + "&app_secret=" + key;
            String sign = SignatureUtils.md5(signContent).toUpperCase();
            map.put("md5_sign", sign);

            raw = HttpUtil.post(normalMchParams.getPayGateway(), map);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("result_code").equals("0")) {
                String payUrl = result.getString("pay_url");
                payUrl = payUrl.replaceAll("\\\\", "");
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
        Map<String, Object> map = new HashMap<>();
        String key = "87dfa538de164d70abc43917f844db49";
        String notifyUrl = "https://www.google.com";

        map.put("app_id", "10259");

        BigDecimal bigDecimal = new BigDecimal(String.valueOf(10000L / 100L));
        DecimalFormat decimalFormat = new DecimalFormat("0.00#");
        String strVal = decimalFormat.format(bigDecimal);

        map.put("amount", strVal);
        map.put("out_trade_id", RandomStringUtils.random(15, true, true));
        map.put("payment_code", 2101);
        map.put("notify_url", notifyUrl);
        map.put("callback_url", notifyUrl);

        String signContent = SignatureUtils.getSignContent(map, null, null) + "&app_secret=" + key;
        String sign = SignatureUtils.md5(signContent).toUpperCase();
        map.put("md5_sign", sign);

        String raw = HttpUtil.post("http://merchant.alipay688.com/api/merchant/order", map);

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
        JSONObject result = JSON.parseObject(raw, JSONObject.class);

    }

}