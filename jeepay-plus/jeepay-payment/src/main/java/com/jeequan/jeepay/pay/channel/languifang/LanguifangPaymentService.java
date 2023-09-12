package com.jeequan.jeepay.pay.channel.languifang;

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
 * 兰桂坊支付
 */
@Service
@Slf4j
public class LanguifangPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "兰桂坊支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.LANGUIFANG;
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

            String merchant_no = normalMchParams.getMchNo();
            String pay_code = normalMchParams.getPayType();


            String order_no = payOrder.getPayOrderId();
            String order_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            long ts = System.currentTimeMillis();

            String callback_url = getNotifyUrl(payOrder.getPayOrderId());
            String callback_type = "POST";

            map.put("merchant_no", merchant_no);
            map.put("pay_code", pay_code);
            map.put("order_no", order_no);
            map.put("order_amount", order_amount);
            map.put("ts", ts);

            String signContent = "key=" + merchant_no + key + "&order_amount=" + order_amount + "&order_no=" + order_no + "&pay_code=" + pay_code + "&ts=" + ts;
            String sign = JeepayKit.md5(signContent).toLowerCase();

            map.put("callback_url", callback_url);
            map.put("callback_type", callback_type);
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
        String key = "155512d37e92584731a7e4dfaa9e2e74";

        String merchant_no = "62642580";
        String pay_code = "112";


        String order_no = RandomStringUtils.random(15, true, true);
        String order_amount = AmountUtil.convertCent2Dollar(10000L);
        long ts = System.currentTimeMillis();

        String callback_url = "https://www.test.com";
        String callback_type = "POST";

        map.put("merchant_no", merchant_no);
        map.put("pay_code", pay_code);
        map.put("order_no", order_no);
        map.put("order_amount", order_amount);
        map.put("ts", ts);

        String signContent = "key=" + merchant_no + key + "&order_amount=" + order_amount + "&order_no=" + order_no + "&pay_code=" + pay_code + "&ts=" + ts;
        String sign = JeepayKit.md5(signContent).toLowerCase();

        map.put("callback_url", callback_url);
        map.put("callback_type", callback_type);
        map.put("sign", sign);

        String payGateway = "http://api.languifangpay.top/v1/api/create_order";

//        raw = HttpUtil.post(normalMchParams.getPayGateway(), map,10000);
        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json") // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}