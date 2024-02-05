package com.jeequan.jeepay.pay.channel.hipay;

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
public class HipayPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[HiPay支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HIPAY;
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

            String paychannelid = normalMchParams.getPayType();
            String recvid = normalMchParams.getMchNo();
            String orderid = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String memuid = SignatureUtils.md5(payOrder.getClientIp());

            map.put("paychannelid", paychannelid);
            map.put("recvid", recvid);
            map.put("orderid", orderid);
            map.put("notifyurl", notifyurl);
            map.put("amount", amount);
            map.put("memuid", memuid);

            //sign=md5("pay"+recvid+orderid+amount+apikey)
            String signStr = "pay" + recvid + orderid + amount + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
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
            if (result.getString("code").equals("1")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("payurl");
                String passageOrderId = data.getString("id");

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
        String key = "8cc93d7321014b21a0f90ff5f77c7f14";

        String paychannelid = "00003";
        String recvid = "662393e834fd54d9298e089a1c5af0ff";
        String orderid = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String notifyurl = "http://www.test.com";
        String memuid = SignatureUtils.md5("127.0.0.1");

        map.put("paychannelid", paychannelid);
        map.put("recvid", recvid);
        map.put("orderid", orderid);
        map.put("notifyurl", notifyurl);
        map.put("amount", amount);
        map.put("memuid", memuid);

        //sign=md5("pay"+recvid+orderid+amount+apikey)
        String signStr = "pay" + recvid + orderid + amount + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "https://xahnob7yqke3.hipay.one/pay/pay/createpay ";
        log.info("[{}]请求:{}", LOG_TAG, map);
        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
