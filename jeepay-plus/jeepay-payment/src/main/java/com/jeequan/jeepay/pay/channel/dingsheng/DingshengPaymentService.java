package com.jeequan.jeepay.pay.channel.dingsheng;

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
public class DingshengPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[鼎盛支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.DINGSHENG;
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

            String partner = normalMchParams.getMchNo();
            String pay_type = normalMchParams.getPayType();

            String out_trade_no = payOrder.getPayOrderId();
            String total_fee = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String exter_invoke_ip = payOrder.getClientIp();

            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;
            String return_type = "1";

            map.put("partner", partner);
            map.put("pay_type", pay_type);
            map.put("out_trade_no", out_trade_no);
            map.put("total_fee", total_fee);
            map.put("exter_invoke_ip", exter_invoke_ip);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();
            map.put("return_type", return_type);
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            raw = HttpUtil.post(payGateway,map,10000);

            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("pay_url");
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
        String key = "a5ef360e3ea9962e1ac4bf48850ea4af";

        String partner = "20151";
        String pay_type = "4";

        String out_trade_no = RandomStringUtils.random(15, true, true);
        String total_fee = AmountUtil.convertCent2DollarShort(10000L);
        String exter_invoke_ip = "127.0.0.1";

        String notify_url = "http://www.test.com";
        String return_url = notify_url;
        String return_type = "1";



        map.put("partner", partner);
        map.put("pay_type", pay_type);
        map.put("out_trade_no", out_trade_no);
        map.put("total_fee", total_fee);
        map.put("exter_invoke_ip", exter_invoke_ip);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);

        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + key;
        String sign = SignatureUtils.md5(signStr).toLowerCase();
        map.put("return_type", return_type);
        map.put("sign", sign);


        String payGateway = "http://baby.pyimu.com/pay/pay/unitesubmit";

        // 发送POST请求并指定JSON数据
        raw = HttpUtil.post(payGateway,map,10000);

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}