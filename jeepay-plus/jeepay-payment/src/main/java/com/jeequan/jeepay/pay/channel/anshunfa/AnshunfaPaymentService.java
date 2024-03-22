package com.jeequan.jeepay.pay.channel.anshunfa;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
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
public class AnshunfaPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[安顺发支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ANSHUNFA;
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
            AnshunfaParamsModel anshunfaParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), AnshunfaParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = anshunfaParamsModel.getSecret();

            String appId = anshunfaParamsModel.getMchNo();
            String orderNo = payOrder.getPayOrderId();
            String groupCode = anshunfaParamsModel.getGroupCode();
            String payType = anshunfaParamsModel.getPayType();
            String currency = "CNY";
            long paidFee = payOrder.getAmount();
            int urlMode = 1;
            String clientIp = payOrder.getClientIp();
            String deviceType = "4";
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String returnUrl = notifyUrl;
            String remark = "7777";

            map.put("appId", appId);
            map.put("orderNo", orderNo);
            map.put("groupCode", groupCode);
            map.put("payType", payType);
            map.put("currency", currency);
            map.put("paidFee", paidFee);
            map.put("urlMode", urlMode);
            map.put("clientIp", clientIp);
            map.put("deviceType", deviceType);
            map.put("notifyUrl", notifyUrl);
            map.put("returnUrl", returnUrl);
            map.put("remark", remark);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = anshunfaParamsModel.getPayGateway();
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
        String key = "GMZX+SG5VXYLEXKNNAPAUQUSVXA";

        String appId = "ADDC2275";
        String orderNo = RandomStringUtils.random(15, true, true);
        String groupCode = "777";
        String payType = "ZFBPAY";
        String currency = "CNY";
        long paidFee = 10000;
        int urlMode = 1;
        String clientIp = "127.0.0.1";
        String deviceType = "4";
        String notifyUrl = "https://www.test.com";
        String returnUrl = notifyUrl;
        String remark = "7777";


        map.put("appId", appId);
        map.put("orderNo", orderNo);
        map.put("groupCode", groupCode);
        map.put("payType", payType);
        map.put("currency", currency);
        map.put("paidFee", paidFee);
        map.put("urlMode", urlMode);
        map.put("clientIp", clientIp);
        map.put("deviceType", deviceType);
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", returnUrl);
        map.put("remark", remark);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://api.518zf.site/order/create";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
