package com.jeequan.jeepay.pay.channel.superpay;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
public class SuperpayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[Super支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SUPERPAY;
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

            String version = "V7";
            String merchant_no = normalMchParams.getMchNo();
            String tran_flow = payOrder.getPayOrderId();
            String tran_datetime = System.currentTimeMillis() + "";
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String pay_type = normalMchParams.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());

            map.put("version", version);
            map.put("merchant_no", merchant_no);
            map.put("tran_flow", tran_flow);
            map.put("tran_datetime", tran_datetime);
            map.put("amount", amount);
            map.put("pay_type", pay_type);
            map.put("notify_url", notify_url);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {

                String payUrl = result.getString("yul1");
                String passageOrderId = result.getString("pay_serial_no");

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
        String key = "f4861a5a82f54acfbde4f3f13736f40cc9b2fc7956d84e579f";

        String version = "V7";
        String merchant_no = "A3_shunfengkeji";
        String tran_flow = RandomStringUtils.random(15, true, true);
        String tran_datetime = System.currentTimeMillis() + "";
        String amount = AmountUtil.convertCent2Dollar(30000L);
        String pay_type = "102";
        String notify_url = "http://www.test.com";

        map.put("version", version);
        map.put("merchant_no", merchant_no);
        map.put("tran_flow", tran_flow);
        map.put("tran_datetime", tran_datetime);
        map.put("amount", amount);
        map.put("pay_type", pay_type);
        map.put("notify_url", notify_url);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "https://dasunapi.trustasia.xyz/merchantinterApi/GJpaymentController/payment";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
