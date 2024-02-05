package com.jeequan.jeepay.pay.channel.shanggu;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class ShangguPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[上古支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SHANGGU;
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

            String mch_id = normalMchParams.getMchNo();
            String pay_type = normalMchParams.getPayType();
            String out_trade_no = payOrder.getPayOrderId();
            String total_fee = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notify_url = URLEncoder.encode(getNotifyUrl(payOrder.getPayOrderId()), StandardCharsets.UTF_8.toString());
            String timestamp = System.currentTimeMillis() / 1000 + "";
            String child_type = "H5";

            map.put("mch_id", mch_id);
            map.put("pay_type", pay_type);
            map.put("out_trade_no", out_trade_no);
            map.put("total_fee", total_fee);
            map.put("notify_url", notify_url);
            map.put("child_type", child_type);
            map.put("timestamp", timestamp);

            map.put("mch_secret", key);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.remove("mch_secret");
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("100")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("url");
                String passageOrderId = data.getString("order_id");

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

    public static void main(String[] args) throws UnsupportedEncodingException {
//        String raw = "";
//
//        Map<String, Object> map = new HashMap<>();
//        String key = "jkjz5u4kRKAqDOLJnfYSRTHZk9nLIFUe";
//
//        String mch_id = "10209";
//        String pay_type = "DigitalRMB";
//        String out_trade_no = RandomStringUtils.random(15, true, true);
//        String total_fee = AmountUtil.convertCent2Dollar(10000L);
//        String notify_url = URLEncoder.encode("http://www.test.com", StandardCharsets.UTF_8.toString());
//        String timestamp = System.currentTimeMillis() / 1000 + "";
//        String child_type = "H5";
//
//        map.put("mch_id", mch_id);
//        map.put("pay_type", pay_type);
//        map.put("out_trade_no", out_trade_no);
//        map.put("total_fee", total_fee);
//        map.put("notify_url", notify_url);
//        map.put("child_type", child_type);
//        map.put("timestamp", timestamp);
//
//        map.put("mch_secret", key);
//
//        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);
//        String sign = SignatureUtils.md5(signStr).toLowerCase();
//        map.remove("mch_secret");
//        map.put("sign", sign);
//
//
//        String payGateway = "http://8.222.70.124/api/gateway/create";
//
//        raw = HttpUtil.post(payGateway, map, 10000);
//        log.info("[{}]请求:{}", LOG_TAG, JSONObject.toJSON(map).toString());
//
//        log.info("[{}]请求响应:{}", LOG_TAG, raw);

        //rOazibTY7gipUkp
        String raw = HttpUtil.post("http://8.222.70.124/pay/Query/rOazibTY7gipUkp", "", 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}