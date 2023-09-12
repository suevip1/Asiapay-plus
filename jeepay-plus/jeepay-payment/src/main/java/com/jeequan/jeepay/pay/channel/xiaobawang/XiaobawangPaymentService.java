package com.jeequan.jeepay.pay.channel.xiaobawang;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
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
public class XiaobawangPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[小霸王支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XIAOBAWANG;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return "";
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

            String uid = normalMchParams.getMchNo();
            String channel = normalMchParams.getPayType();
            String outtradeno = payOrder.getPayOrderId();
            long fee = payOrder.getAmount();

            String returnUrl = getNotifyUrl(payOrder.getPayOrderId());
            String selectUrl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("uid", uid);
            map.put("channel", channel);
            map.put("outtradeno", outtradeno);
            map.put("fee", fee);

            map.put("returnUrl", returnUrl);
            map.put("selectUrl", selectUrl);

            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map,10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0000")) {
                String payUrl = result.getString("payurl");
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
        String key = "BD35F7BD037F7FCC675313324F414009";

        String uid = "549";
        String channel = "28";
        String outtradeno = RandomStringUtils.random(15, true, true);
        long fee = 1000L;

        String returnUrl = "https://www.test.com";
        String selectUrl = "https://www.test.com";

        map.put("uid", uid);
        map.put("channel", channel);
        map.put("outtradeno", outtradeno);
        map.put("fee", fee);

        map.put("returnUrl", returnUrl);
        map.put("selectUrl", selectUrl);

        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "http://api.wiujdw.com/v1/payment";

        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
        JSONObject result = JSON.parseObject(raw, JSONObject.class);
        log.info("[{}]请求响应:{}", LOG_TAG, result.toJSONString());
    }
}