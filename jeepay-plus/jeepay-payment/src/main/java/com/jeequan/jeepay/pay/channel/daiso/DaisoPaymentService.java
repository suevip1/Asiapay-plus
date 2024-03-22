package com.jeequan.jeepay.pay.channel.daiso;

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
public class DaisoPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[Daiso支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.DAISO;
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

            Map<String, String> mapHead = new HashMap<>();
            Map<String, Object> map = new HashMap<>();
            String key = normalMchParams.getSecret();

            String MerId = normalMchParams.getMchNo();
            String merchantOrderNo = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String playerName = "palyer";
            String realName = "real";
            String notifyUrl = getNotifyUrl(payOrder.getPayOrderId());
            String createTime = (Long.valueOf(System.currentTimeMillis() / 1000L)) + "";
            String payType = normalMchParams.getPayType();

            mapHead.put("MerId", MerId);
            map.put("merchantOrderNo", merchantOrderNo);
            map.put("amount", amount);
            map.put("playerName", playerName);
            map.put("realName", realName);
            map.put("notifyUrl", notifyUrl);
            map.put("createTime", createTime);
            map.put("payType", payType);

            String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
            String sign = SignatureUtils.md5(SignStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).headerMap(mapHead, false).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("Code").equals("210")) {

                String payUrl = result.getString("Data");
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

        Map<String, String> mapHead = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        String key = "662ad055-575b-48fa-9cd3-18a5edd6eb51";

        String MerId = "26707";
        String merchantOrderNo = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2DollarShort(1000L);
        String playerName = "palyer";
        String realName = "real";
        String notifyUrl = "http://www.test.com";
        String createTime = (Long.valueOf(System.currentTimeMillis() / 1000L)) + "";
        String payType = "325";

        mapHead.put("MerId", MerId);
        map.put("merchantOrderNo", merchantOrderNo);
        map.put("amount", amount);
        map.put("playerName", playerName);
        map.put("realName", realName);
        map.put("notifyUrl", notifyUrl);
        map.put("createTime", createTime);
        map.put("payType", payType);

        String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
        String sign = SignatureUtils.md5(SignStr).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://www.htpay.cc/Api/v1/Recharge/pay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).headerMap(mapHead, false).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, JSONObject.toJSONString(raw));
    }
}