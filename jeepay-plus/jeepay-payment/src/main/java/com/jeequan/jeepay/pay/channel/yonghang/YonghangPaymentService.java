package com.jeequan.jeepay.pay.channel.yonghang;

import cn.hutool.http.HttpResponse;
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


@Service
@Slf4j
public class YonghangPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[永航支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YONGHANG;
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

            String submit_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String order_no = payOrder.getPayOrderId();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());

            String app_id = normalMchParams.getMchNo();
            String time = System.currentTimeMillis() / 1000 + "";
            String pay_type = normalMchParams.getPayType();

            map.put("submit_amount", submit_amount);
            map.put("order_no", order_no);
            map.put("notify_url", notify_url);
            map.put("app_id", app_id);
            map.put("time", time);
            map.put("pay_type", pay_type);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String sign = SignatureUtils.md5((signContent + "&key=" + key).toUpperCase()).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0") && result.getString("message").equalsIgnoreCase("success")) {

                String payUrl = result.getJSONObject("data").getString("pay_url");
                String passageOrderId = result.getJSONObject("data").getString("local_no");

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
        String key = "ijTPsbeiYXCGAtysR5M7mRfzZ2SFhrBcpWcWppRP4PE3JMMQbmPznD7MPycTXnyD";

        String submit_amount = AmountUtil.convertCent2Dollar(39900L);
        String order_no = RandomStringUtils.random(15, true, true);
        String notify_url = "https://www.test.com";

        String app_id = "xinels";
        String time = System.currentTimeMillis() / 1000 + "";
        String pay_type = "399";

        map.put("submit_amount", submit_amount);
        map.put("order_no", order_no);
        map.put("notify_url", notify_url);
        map.put("app_id", app_id);
        map.put("time", time);
        map.put("pay_type", pay_type);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String sign = SignatureUtils.md5((signContent + "&key=" + key).toUpperCase()).toLowerCase();
        map.put("sign", sign);

        String payGateway = " http://144.34.191.105:10081/sdk/orders";
        log.info("[{}]请求:{}", LOG_TAG, map);

//        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();

//        raw = HttpUtil.post(normalMchParams.getPayGateway(), map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}