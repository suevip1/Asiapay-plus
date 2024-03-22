package com.jeequan.jeepay.pay.channel.ltfpay;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class LtfpayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[LTF支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.LTFPAY;
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

            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String custSeq = payOrder.getPayOrderId();
            String extend = "";
            String gateway_id = normalMchParams.getPayType();
            String outMchntId = normalMchParams.getMchNo();
            String return_url = getNotifyUrl(payOrder.getPayOrderId());
            String remark = payOrder.getClientIp();

            map.put("amount", amount);
            map.put("custSeq", custSeq);
            map.put("extend", extend);
            map.put("gateway_id", gateway_id);
            map.put("outMchntId", outMchntId);
            map.put("remark", remark);
            map.put("return_url", URLEncoder.encode(return_url, "UTF-8"));

            String signStr = SignatureUtils.getSignContent(map, null, null);
            String signStrA = SignatureUtils.md5(signStr).toLowerCase();
            String sign = SignatureUtils.md5(signStrA + key).toLowerCase();
            map.put("sign", sign);
            map.put("return_url", return_url);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("state").equals("1")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("payUrl");
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


    public static void main(String[] args) throws UnsupportedEncodingException {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "I68DJESgAP3GbN25kTcysWw74xHVKQ0r";

        String amount = AmountUtil.convertCent2Dollar(100L);
        String custSeq = RandomStringUtils.random(15, true, true);
        String extend = "";
        String gateway_id = "13";
        String outMchntId = "24D1EDC727E43F1DE68FDA980359E09B";
        String remark = "127.0.0.1";
        String return_url = "https://www.test.com";

        map.put("amount", amount);
        map.put("custSeq", custSeq);
        map.put("extend", extend);
        map.put("gateway_id", gateway_id);
        map.put("outMchntId", outMchntId);
        map.put("remark", remark);
        map.put("return_url", URLEncoder.encode(return_url, "UTF-8"));

        String signStr = SignatureUtils.getSignContent(map, null, null);
        String signStrA = SignatureUtils.md5(signStr).toLowerCase();
        String sign = SignatureUtils.md5(signStrA + key).toLowerCase();
        map.put("sign", sign);
        map.put("return_url", return_url);

        String payGateway = "http://124.71.7.146:4026/pay/index/dopay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}