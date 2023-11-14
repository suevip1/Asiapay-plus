package com.jeequan.jeepay.pay.channel.hengsheng;

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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * 恒生支付
 */
@Service
@Slf4j
public class HengshengPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[恒生支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HENGSHENG;
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
            HengshengParamsModel hengshengParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), HengshengParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String requestPrivateKey= hengshengParamsModel.getRequestPrivateKey();

            String merchantId = hengshengParamsModel.getMchNo();
            String version = "1.0.0";
            String merchantOrderNo = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String model = hengshengParamsModel.getPayType();
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("merchantId", merchantId);
            map.put("version", version);
            map.put("merchantOrderNo", merchantOrderNo);
            map.put("amount", amount);
            map.put("model", model);
            map.put("notifyUrl", notifyUrl);

            String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
            String sign = SignatureUtils.buildSHA1WithRSASignByPrivateKey(signContent, requestPrivateKey);
            map.put("sign", sign);

            String payGateway = hengshengParamsModel.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {

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


    public static void main(String[] args) throws UnsupportedEncodingException {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "";
        String rsa = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIZTJVMjg+0+48EVFwcd280z4fG8pHsTf4T2pb9b/fZ5dB4aP1bzPSYSnz0CXHTUMMg4ayyvbSRxoa1f3SvjXUsyDZVRL7fp6xg8Py10vsKrUAb/BmepgsE0ArGglyHbsRzqxUUjdMYWCdgtoIoSnl590MQ1vZHr++RQDTI1yee9AgMBAAECgYB6trsCmQ+NTkcDviCrf7uuXBeuoIgjaEy3TB1gFMqOwaYzWgdDiDP09E5FjkKo+LEYOXKwABPVl3n8yfdkgPJYyd0esDP1Bn0Ic8eQ4SJZiWKjReuhnsf2pszM+b2vzqHoe2WJaqWmjrFtGrEtDIoyFEYSHtVaBTdIRCopX+r8AQJBANVGY4+b3QKlo23qnrrGdsggjerLKc0Bv5Cr0UpIebckWwBDP9Uxwg5NwBpCaGydkPNBYNfanw4zTdjw/oq7kXUCQQChO96lctucfM59zv8F02Bcbc+nHjPUHdpxYiX0BWobt58siNdmalkMa8us5Jj4RcTKfbXZ9H4teoorpALqUawpAkEA01yHzMnF1Rq8hXEQT7/h9eG1Y6xmR8JkzAZLSxfacDL6cJ81Ap7mV3CcYinP/VyGS11OkX0bRmJ30vi6+lh7BQJAMrRjRmCik8nkHkh0ht58lVSrUwD6h6CDy9hz2xPA1MqgMh8urjSPoIpd2Rdiy+EVDCEQiWSMMb0xqpu/lvTsUQJBAKSI//exmG5duWcJ8wgV8VYV9IHyhSE1emd1DMdeV6YE8SiBxnbQsKvG+tfbqe/qDnB6XI3+PTJ56xhSiKOEruw=";

        String merchantId = "10055";
        String version = "1.0.0";
        String merchantOrderNo = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String model = "423";
        String notifyUrl = "https://www.test.com";

        map.put("merchantId", merchantId);
        map.put("version", version);
        map.put("merchantOrderNo", merchantOrderNo);
        map.put("amount", amount);
        map.put("model", model);
        map.put("notifyUrl", notifyUrl);

        String signContent = SignatureUtils.getSignContent(map, null, new String[]{""});
        String sign = SignatureUtils.buildSHA1WithRSASignByPrivateKey(signContent, rsa);
        map.put("sign", sign);

        String payGateway = "https://gateway.hhspay.com/api/payOrder/create";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
