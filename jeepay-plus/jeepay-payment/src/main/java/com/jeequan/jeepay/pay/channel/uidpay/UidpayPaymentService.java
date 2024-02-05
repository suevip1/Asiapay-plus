package com.jeequan.jeepay.pay.channel.uidpay;

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
public class UidpayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[UID支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.UIDPAY;
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

            String merchantNo = normalMchParams.getMchNo();
            String productCode = normalMchParams.getPayType();
            String merchantOrderNo = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("merchantNo", merchantNo);
            map.put("productCode", productCode);
            map.put("merchantOrderNo", merchantOrderNo);
            map.put("amount", amount);
            map.put("notifyUrl", notifyUrl);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String sign = SignatureUtils.md5(signContent + "&key=" + key).toLowerCase();
            map.put("sign", sign);


            String payGateway = normalMchParams.getPayGateway();

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
                String payUrl = data.getString("payUrl");
                String passageOrderId = data.getString("orderNo");

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
        String key = "65984e22c0027822479ab916";

        String merchantNo = "330074";
        String productCode = "97";
        String merchantOrderNo = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2DollarShort(10000L);
        String notifyUrl = "http://www.test.com";

        map.put("merchantNo", merchantNo);
        map.put("productCode", productCode);
        map.put("merchantOrderNo", merchantOrderNo);
        map.put("amount", amount);
        map.put("notifyUrl", notifyUrl);


        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String sign = SignatureUtils.md5(signContent + "&key=" + key).toLowerCase();
        map.put("sign", sign);


        String payGateway = "http://103.78.243.19/uidapi/pay/v1/payment/create";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
