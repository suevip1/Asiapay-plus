package com.jeequan.jeepay.pay.channel.jiuyipay2;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class Jiuyipay2PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[玖弈支付2]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.JIUYIPAY2;
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

            String fxid = normalMchParams.getMchNo();
            String fxddh = payOrder.getPayOrderId();
            String fxdesc = "goods";
            String fxfee = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String fxnotifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String fxbackurl = fxnotifyurl;
            String fxpay = normalMchParams.getPayType();
            String fxip = payOrder.getClientIp();
            String fxuserid = fxddh;


            map.put("fxid", fxid);
            map.put("fxddh", fxddh);
            map.put("fxdesc", fxdesc);
            map.put("fxfee", fxfee);
            map.put("fxnotifyurl", fxnotifyurl);
            map.put("fxbackurl", fxbackurl);
            map.put("fxpay", fxpay);
            map.put("fxip", fxip);
            map.put("fxuserid", fxuserid);

            List<String> fieldOrder = Arrays.asList("fxid", "fxddh", "fxfee", "fxnotifyurl");
            StringBuilder sb = new StringBuilder();
            for (String field : fieldOrder) {
                if (map.containsKey(field)) {
                    Object value = map.get(field);
                    sb.append(field).append("=").append(value).append("&");
                }
            }
            sb.append(key);
            String fxsign = SignatureUtils.md5(sb.toString()).toLowerCase();
            map.put("fxsign", fxsign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map,10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("status").equals("1")) {

                String payUrl = result.getString("payurl");
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
        String key = "GrAEsjNNLlCxzcGJdzlDeAhXqmPLbxwh";

        String fxid = "183";
        String fxddh = RandomStringUtils.random(15, true, true);
        String fxdesc = "fxdesc";
        String fxfee = AmountUtil.convertCent2Dollar(10000L);
        String fxnotifyurl = "https://www.test.com";
        String fxbackurl = fxnotifyurl;
        String fxpay = "1074";
        String fxip = "127.0.0.1";
        String fxuserid = fxddh;

        map.put("fxid", fxid);
        map.put("fxddh", fxddh);
        map.put("fxdesc", fxdesc);
        map.put("fxfee", fxfee);
        map.put("fxnotifyurl", fxnotifyurl);
        map.put("fxbackurl", fxbackurl);
        map.put("fxpay", fxpay);
        map.put("fxip", fxip);
        map.put("fxuserid", fxuserid);

        List<String> fieldOrder = Arrays.asList("fxid", "fxddh", "fxfee", "fxnotifyurl");
        StringBuilder sb = new StringBuilder();
        for (String field : fieldOrder) {
            if (map.containsKey(field)) {
                Object value = map.get(field);
                sb.append(field).append("=").append(value).append("&");
            }
        }
        sb.append(key);
        String fxsign = SignatureUtils.md5(sb.toString()).toLowerCase();
        map.put("fxsign", fxsign);

        String payGateway = "https://www.hnpay8.com/Pay";

        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}