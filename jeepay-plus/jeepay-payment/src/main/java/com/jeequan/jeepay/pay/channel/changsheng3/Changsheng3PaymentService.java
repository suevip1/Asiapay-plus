package com.jeequan.jeepay.pay.channel.changsheng3;

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
public class Changsheng3PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[昌盛支付3]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.CHANGSHENG3;
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

            String user_id = normalMchParams.getMchNo();
            String money = AmountUtil.convertCent2Dollar(payOrder.getAmount());

            String type = normalMchParams.getPayType();
            String user_order_no = payOrder.getPayOrderId();

            String timestamp = (Long.valueOf(System.currentTimeMillis() / 1000L)) + "";
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());


            map.put("user_id", user_id);
            map.put("money", money);
            map.put("type", type);
            map.put("user_order_no", user_order_no);
            map.put("timestamp", timestamp);
            map.put("notify_url", notify_url);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1")) {
                String payUrl = result.getString("pay_url");
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

        Map<String, Object> map = new HashMap<>();
        String key = "IjU2wgbrk3Frj7HhyK36REzebHIUX3Za8BDUd3kezSjBdckeBREwPsV6N4ZymT5S";

        String user_id = "9113777112";
        String money = AmountUtil.convertCent2Dollar(20000L);
        String type = "1";
        String user_order_no = RandomStringUtils.random(15, true, true);
        String timestamp = (Long.valueOf(System.currentTimeMillis() / 1000L)) + "";
        String notify_url = "https://www.test.com";


        map.put("user_id", user_id);
        map.put("money", money);
        map.put("type", type);
        map.put("user_order_no", user_order_no);
        map.put("timestamp", timestamp);
        map.put("notify_url", notify_url);


        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("sign", sign);
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));

        String payGateway = "http://120.77.31.147/index/api/order";
        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}