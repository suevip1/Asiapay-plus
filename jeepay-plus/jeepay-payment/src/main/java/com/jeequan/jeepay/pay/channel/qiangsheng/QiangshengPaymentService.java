package com.jeequan.jeepay.pay.channel.qiangsheng;

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

/**
 * 强盛支付
 */
@Service
@Slf4j
public class QiangshengPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[强盛支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.QIANGSHENG;
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
            String order_no = payOrder.getPayOrderId();
            Long amount = payOrder.getAmount();
            String pay_type = normalMchParams.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());

            map.put("uid", uid);
            map.put("order_no", order_no);
            map.put("amount", amount);
            map.put("pay_type", pay_type);
            map.put("notify_url", notify_url);

            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map,10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {

                String payUrl = result.getString("data");
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
        String key = "2478f9b0bf1a6491499a4956718f882b";

        String uid = "12178444";
        String order_no = RandomStringUtils.random(15, true, true);
        String amount = "10000";
        String pay_type = "8005";
        String notify_url = "http://www.test.com";


        map.put("uid", uid);
        map.put("order_no", order_no);
        map.put("amount", amount);
        map.put("pay_type", pay_type);
        map.put("notify_url", notify_url);

        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);


        String payGateway = "http://18.183.219.217:8080/3/api/pay/order";

        raw = HttpUtil.post(payGateway, map,10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
