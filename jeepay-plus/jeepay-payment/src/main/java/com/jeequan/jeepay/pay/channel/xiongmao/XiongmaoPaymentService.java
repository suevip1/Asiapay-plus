package com.jeequan.jeepay.pay.channel.xiongmao;

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

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class XiongmaoPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[熊猫支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XIONGMAO;
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
            XiongmaoParamsModel xiongmaoParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), XiongmaoParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = xiongmaoParamsModel.getSecret();

            String merchant_id = xiongmaoParamsModel.getMchNo();
            String order_no = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String code = xiongmaoParamsModel.getPayType();
            String type = xiongmaoParamsModel.getTypeCode();
            String notice_url = getNotifyUrl(payOrder.getPayOrderId());

            map.put("merchant_id", merchant_id);
            map.put("order_no", order_no);
            map.put("amount", amount);
            map.put("code", code);
            map.put("type", type);
            map.put("notice_url", notice_url);

            String signContent = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""});
            String sign = SignatureUtils.md5(signContent + "&sign=" + key).toUpperCase();
            map.put("sign", sign);

            String payGateway = xiongmaoParamsModel.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");

                String payUrl = data.getString("pay_url");
                String passageOrderId = data.getString("order_id");

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
        String key = "nyAJbcS5I282cUbOStyTo6pAA8IxKYrI";

        String merchant_id = "2695";
        String order_no = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String code = "XSKM";
        String type = "1";
        String notice_url = "http://www.test.com";

        map.put("merchant_id", merchant_id);
        map.put("order_no", order_no);
        map.put("amount", amount);
        map.put("code", code);
        map.put("type", type);
        map.put("notice_url", notice_url);

        String signContent = SignatureUtils.getSignContentFilterEmpty(map, new String[]{""});
        String sign = SignatureUtils.md5(signContent + "&sign=" + key).toUpperCase();
        map.put("sign", sign);

        String payGateway = "https://abc.xiongmao6.com/index/order";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}