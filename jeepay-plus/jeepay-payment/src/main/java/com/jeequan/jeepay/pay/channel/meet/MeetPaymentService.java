package com.jeequan.jeepay.pay.channel.meet;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
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
public class MeetPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[Meet支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.MEET;
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
            MeetParamsModel meetParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), MeetParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = meetParamsModel.getSecret();

            String mem_id = meetParamsModel.getMchNo();
            String trade_no = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String way = meetParamsModel.getWay();
            String payment = meetParamsModel.getPayment();
            String code = meetParamsModel.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;

            map.put("mem_id", mem_id);
            map.put("trade_no", trade_no);
            map.put("money", money);
            map.put("way", way);
            map.put("payment", payment);
            map.put("code", code);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&apikey=" + key;
            String sign = SignatureUtils.md5(signStr).toUpperCase();
            map.put("sign", sign);

            String payGateway = meetParamsModel.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
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
        String key = "2cf873a7fefab9739a24b083023d85d0";

        String mem_id = "100054";
        String trade_no = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2Dollar(900L);
        String way = "h5";
        String payment = "alipay";
        String code = "ZfbXeUid";
        String notify_url = "http://www.test.com";
        String return_url = notify_url;

        map.put("mem_id", mem_id);
        map.put("trade_no", trade_no);
        map.put("money", money);
        map.put("way", way);
        map.put("payment", payment);
        map.put("code", code);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&apikey=" + key;
        String sign = SignatureUtils.md5(signStr).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://pay.meet-pay.com/v1/pay/create";
        log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
