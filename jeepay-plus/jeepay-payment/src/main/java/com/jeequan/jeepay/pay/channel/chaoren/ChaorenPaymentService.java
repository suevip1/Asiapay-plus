package com.jeequan.jeepay.pay.channel.chaoren;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.HttpClientPoolUtil;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class ChaorenPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[超人支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.CHAOREN;
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
            ChaoRenParamsModel chaoRenParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), ChaoRenParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = chaoRenParamsModel.getRequestPrivateKey().replaceAll("\\s+", "");

            String token = chaoRenParamsModel.getMchNo();
            String channel = chaoRenParamsModel.getChannelId();
            String pay_type = chaoRenParamsModel.getPayType();

            int amount = (int) (payOrder.getAmount() / 100);
            String out_trade_no = payOrder.getPayOrderId();

            map.put("token", token);
            map.put("channel", channel);
            map.put("pay_type", URLEncoder.encode(pay_type, "UTF-8"));
            map.put("amount", amount);
            map.put("out_trade_no", out_trade_no);
            map.put("callback_url", URLEncoder.encode(getNotifyUrl(payOrder.getPayOrderId()), "UTF-8"));

            String signContent = out_trade_no+amount;
            String sign = SignatureUtils.buildRSASHA1SignByPrivateKey(signContent, key);
            map.put("sign", URLEncoder.encode(sign, "UTF-8"));

            String payGateway = chaoRenParamsModel.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpClientPoolUtil.doGet(payGateway+"?"+JeepayKit.genUrlParams(map));
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1")) {
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

        Map<String, Object> map = new LinkedHashMap<>();
        String key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAMRKFs/OSf3SpWAYbSfpR+ApIrEVrtP9+uWgghMCZ56OSEiu/qfUR5rKM2EonkbY6sgVbChejm9I/aIM0YLJWQzgAS5gP5si+oCFq2DhSKzFH8yJhjARdz2HhcAr7laa0YJJ8wwAed7E4JcoofVafTRbyYjCDnAQtnWQIs4haxEZAgMBAAECf0voWuoB3JtL2qhOyeelTyZgwgm5WOKiVWR3rdWsGGY4n7t78P72GLrDdxsbmhnLyP+XSTxTVMG0eGEyk9MACm9ClY1YoqA5lYImYOorLWDq0X48Zh50oNv8oBrTledCJGDkOaukDl15wJ1K4cwJdq/swwhNOMQBcfKM44hE4J0CQQDsBC0ZK5khj+ci3DF/6QiXj7Vj1+tltpzPda34NuV86ZQeFoH/8gYEMMKeeYfY9CnxafT1XIWMXL+4y0HukoXvAkEA1OjNvYsHieyU5vr6P4UDnD7eHjPQrv3fxlMRCI7ERJOJPGa45MsQREL9zBtEV3zTG6LrjfolZ1xq3O/mS6UhdwJAPYYpK3KODI2ytwenb6yTrM6tfkV++5jK79nBa/8De7h4AA/l+45fux/q29zaaNfdRy1TydJAjgJviACUQ1i2yQJBAINxJnN6PbE8ycC/+Xb1m4D+nwpuWstuGJTL+5wY57qeiv8rO8KkSuqZoS49Nzf77CkHr/z7DdR+DctQoBDbZYkCQQCbR+wS3sHIdLfX9iVQ3pdpN9lbfzabSu0cGQZlqzeNSzL3tYkZYDn8hpvEb8/HRi0z+ktOQSdLSlsQ+MPQhVbi";

        String token = "a8943464d134b2f7bbe59717bbe2d15e";
        String channel = "swqj";
        String pay_type = "微信";
        int amount = (int) (3000L / 100);
        String out_trade_no = RandomStringUtils.random(15, true, true);
        String callback_url = "https://www.test.com";

        map.put("token", token);
        map.put("channel", channel);
        map.put("pay_type", URLEncoder.encode(pay_type, "UTF-8"));
        map.put("amount", amount);
        map.put("out_trade_no", out_trade_no);
        map.put("callback_url", URLEncoder.encode(callback_url, "UTF-8"));
        String signContent = out_trade_no+amount;
        String sign = SignatureUtils.buildRSASHA1SignByPrivateKey(signContent, key);
        map.put("sign", URLEncoder.encode(sign, "UTF-8"));

        String payGateway = "http://49.234.152.205:8888/api/createOrder";

        raw = HttpClientPoolUtil.doGet(payGateway+"?"+JeepayKit.genUrlParams(map));
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}