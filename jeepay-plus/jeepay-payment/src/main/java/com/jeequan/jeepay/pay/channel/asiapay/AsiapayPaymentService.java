package com.jeequan.jeepay.pay.channel.asiapay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class AsiapayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "亚洲支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ASIAPAY;
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
            long reqTime = System.currentTimeMillis();

            map.put("mchNo", normalMchParams.getMchNo());
            map.put("mchOrderNo", payOrder.getPayOrderId());
            map.put("amount", payOrder.getAmount());
            map.put("productId", normalMchParams.getPayType());
            map.put("reqTime", reqTime);
            map.put("clientIp", payOrder.getClientIp());
            map.put("notifyUrl", getNotifyUrl(payOrder.getPayOrderId()));

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map,10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            JSONObject data = result.getJSONObject("data");

            //拉起订单成功
            if (result.getString("code").equals("0") && StringUtils.isNotEmpty(data.getString("payData"))) {

                String payUrl = data.getString("payData");
                String passageOrderId = data.getString("payOrderId");

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
        Map<String, Object> map = new HashMap<>();
        String key = "XFjGbHNUho8MqYWycYsNFzSwBVvOAIepDz1QDTfoIfKFojXVyTFHReNteWzGSVoXyXLJzDAo9p0G6GIjQYPAPJXSttlymJ9pqvaVUsna8v6cRezJvmTSJleEPImlTqDI";
        String mchNo = "M1691231056";

        map.put("mchNo", mchNo);
        map.put("mchOrderNo", "AT1202309052102532632");
        map.put("amount", 5000L);
        map.put("productId", "1000");
        map.put("reqTime", "1693918973130");
        map.put("clientIp", "127.0.0.1");
        map.put("notifyUrl", "http://107.148.198.70:8403/api/klnzfbjw/notify");
        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String  raw = HttpUtil.post("http://pay-api.kln-mobile.com/api/pay/unifiedOrder", map);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}