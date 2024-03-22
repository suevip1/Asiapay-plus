package com.jeequan.jeepay.pay.channel.jinli;

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
public class JinliPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "锦鲤支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.JINLI;
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

            String merchantid = normalMchParams.getMchNo();
            String type = normalMchParams.getPayType();
            String orderid = payOrder.getPayOrderId();
            String value = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String callbackurl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("merchantid", merchantid);
            map.put("type", type);
            map.put("orderid", orderid);
            map.put("value", value);
            map.put("callbackurl", callbackurl);
            String signStr = "merchantid=" + merchantid + "&type=" + type + "&value=" + value + "&orderid=" + orderid + "&callbackurl=" + callbackurl + key;
            String sign = SignatureUtils.md5(signStr);
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("payload");
                String payUrl = data.getString("data");

                String passageOrderId = result.getString("sysorderid");

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
        String key = "fsXZ3a4Cc7r6EHSb36Gjxw26cTRTCW8n";

        String merchantid = "80024601";
        String type = "1000";
        String orderid = RandomStringUtils.random(15, true, true);
        String value = AmountUtil.convertCent2Dollar(10000L);
        String callbackurl = "https://www.test.com";

        map.put("merchantid", merchantid);
        map.put("type", type);
        map.put("orderid", orderid);
        map.put("value", value);
        map.put("callbackurl", callbackurl);
        String signStr = "merchantid=" + merchantid + "&type=" + type + "&value=" + value + "&orderid=" + orderid + "&callbackurl=" + callbackurl + key;
        String sign = SignatureUtils.md5(signStr);
        map.put("sign", sign);

        String payGateway = "http://www.koipayment.net:8896/api/order/create";

        raw = HttpUtil.post(payGateway, map, 10000);
        // 发送POST请求并指定JSON数据
//        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json") // 指定请求体的Content-Type为JSON.execute();
        // 处理响应
//        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}