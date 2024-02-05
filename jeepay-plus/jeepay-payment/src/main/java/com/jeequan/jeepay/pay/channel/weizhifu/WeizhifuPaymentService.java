package com.jeequan.jeepay.pay.channel.weizhifu;

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
 * 微支付
 */
@Service
@Slf4j
public class WeizhifuPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[微支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WEIZHIFU;
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

            String version = "1.0";
            String customerid = normalMchParams.getMchNo();
            String sdorderno = payOrder.getPayOrderId();
            String total_fee = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String paytype = normalMchParams.getPayType();
            String notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String returnurl = notifyurl;

            map.put("version", version);
            map.put("customerid", customerid);
            map.put("sdorderno", sdorderno);
            map.put("total_fee", total_fee);
            map.put("paytype", paytype);
            map.put("notifyurl", notifyurl);
            map.put("returnurl", returnurl);

            String signStr = "version=" + version + "&customerid=" + customerid + "&total_fee=" + total_fee
                    + "&sdorderno=" + sdorderno + "&notifyurl=" + notifyurl+ "&returnurl=" + returnurl + "&" + key;
            String sign = SignatureUtils.md5(signStr);
            map.put("sign", sign);
            map.put("jsonformat", 1);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("ok")) {

                String payUrl = result.getString("url");
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
        String key = "ca64c99f1b96dc45077b7a2ca1191f76f0f88f64";

        String version = "1.0";
        String customerid = "10999";
        String sdorderno = RandomStringUtils.random(15, true, true);
        String total_fee = AmountUtil.convertCent2Dollar(20000L);
        String paytype = "crzfb";
        String notifyurl = "http://www.test.com";
        String returnurl = notifyurl;

        map.put("version", version);
        map.put("customerid", customerid);
        map.put("sdorderno", sdorderno);
        map.put("total_fee", total_fee);
        map.put("paytype", paytype);
        map.put("notifyurl", notifyurl);
        map.put("returnurl", returnurl);

        String signStr = "version=" + version + "&customerid=" + customerid + "&total_fee=" + total_fee
                + "&sdorderno=" + sdorderno + "&notifyurl=" + notifyurl+ "&returnurl=" + returnurl + "&" + key;
        log.info("[{}]请求加密串:{}", LOG_TAG, signStr);
        String sign = SignatureUtils.md5(signStr);
        log.info("[{}]请求sign:{}", LOG_TAG, sign);
        map.put("sign", sign);
        map.put("jsonformat", 1);

        String payGateway = "http://47.74.50.99/apisubmit";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
