package com.jeequan.jeepay.pay.channel.feiyue;

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


/**
 * 飞跃支付
 */
@Service
@Slf4j
public class FeiyuePaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "飞跃支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.FEIYUE;
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

            String merchant_no = normalMchParams.getMchNo();
            String pay_code = normalMchParams.getPayType();
            String order_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String order_no = payOrder.getPayOrderId();
            String callback_url = getNotifyUrl(payOrder.getPayOrderId());
            String attach = "attach";
            long ts = System.currentTimeMillis();

            map.put("merchant_no", merchant_no);
            map.put("pay_code", pay_code);
            map.put("order_amount", order_amount);
            map.put("order_no", order_no);
            map.put("callback_url", callback_url);
            map.put("attach", attach);
            map.put("ts", ts);

            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();

            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200") && result.getString("success").equals("true")) {
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
        String key = "72bc0398a0d0f67b15d4109e78b2a689";

        String merchant_no = "81712049";
        String pay_code = "145582";
        String order_amount = AmountUtil.convertCent2Dollar(30000L);
        String order_no = RandomStringUtils.random(15, true, true);
        String callback_url = "https://www.test.com";
        String attach = "attach";
        long ts = System.currentTimeMillis();

        map.put("merchant_no", merchant_no);
        map.put("pay_code", pay_code);
        map.put("order_amount", order_amount);
        map.put("order_no", order_no);
        map.put("callback_url", callback_url);
        map.put("attach", attach);
        map.put("ts", ts);

        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://api.dstayy.com/v2/order/add";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}