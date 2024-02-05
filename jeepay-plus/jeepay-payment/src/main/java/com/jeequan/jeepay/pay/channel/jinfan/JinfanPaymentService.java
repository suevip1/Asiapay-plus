package com.jeequan.jeepay.pay.channel.jinfan;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 金帆支付
 */
@Service
@Slf4j
public class JinfanPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[金帆支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.JINFAN;
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
            String channel = normalMchParams.getPayType();
            String out_order_no = payOrder.getPayOrderId();
            String money = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String client_ip = payOrder.getClientIp();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            long timestamp = System.currentTimeMillis();

            map.put("mch_id", mch_id);
            map.put("channel", channel);
            map.put("out_order_no", out_order_no);
            map.put("money", money);
            map.put("client_ip", client_ip);
            map.put("notify_url", notify_url);
            map.put("timestamp", timestamp);

            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();

            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
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
        String key = "42CDC2B70B30002962F57001B0DA2EE4";

        String mch_id = "205";
        String channel = "alipay";
        String out_order_no = RandomStringUtils.random(15, true, true);
        String money = AmountUtil.convertCent2Dollar(100L);
        String client_ip = "127.0.0.1";
        String notify_url = "https://www.test.com";
        long timestamp = System.currentTimeMillis();

        map.put("mch_id", mch_id);
        map.put("channel", channel);
        map.put("out_order_no", out_order_no);
        map.put("money", money);
        map.put("client_ip", client_ip);
        map.put("notify_url", notify_url);
        map.put("timestamp", timestamp);

        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://api.3a.cash/api/pay/unifiedorder";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}