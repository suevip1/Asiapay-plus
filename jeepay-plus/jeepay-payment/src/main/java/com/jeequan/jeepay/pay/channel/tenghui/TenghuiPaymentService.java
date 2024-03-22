package com.jeequan.jeepay.pay.channel.tenghui;

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

@Service
@Slf4j
public class TenghuiPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[腾辉支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.TENGHUI;
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
            String mch_order_no = payOrder.getPayOrderId();
            String pay_money = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String pay_product_id = normalMchParams.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String callback_url = notify_url;

            map.put("mch_id", mch_id);
            map.put("mch_order_no", mch_order_no);
            map.put("pay_money", pay_money);
            map.put("pay_product_id", pay_product_id);
            map.put("notify_url", notify_url);
            map.put("callback_url", callback_url);

            String firstSign = JeepayKit.getSign(map, key).toLowerCase();
            String sign = SignatureUtils.md5(firstSign + key).toUpperCase();
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
        String key = "b1og0ef6r8q7k08aetrsm3dz5yyvfdbt";

        String mch_id = "6209";
        String mch_order_no = RandomStringUtils.random(15, true, true);
        String pay_money = AmountUtil.convertCent2Dollar(10000L);
        String pay_product_id = "8024";
        String notify_url = "http://www.test.com";
        String callback_url = notify_url;

        map.put("mch_id", mch_id);
        map.put("mch_order_no", mch_order_no);
        map.put("pay_money", pay_money);
        map.put("pay_product_id", pay_product_id);
        map.put("notify_url", notify_url);
        map.put("callback_url", callback_url);

        String firstSign = JeepayKit.getSign(map, key).toLowerCase();
        String sign = SignatureUtils.md5(firstSign + key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "https://www.99091.top/api/pay";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}