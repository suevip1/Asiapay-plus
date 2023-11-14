package com.jeequan.jeepay.pay.channel.cangqiong;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 苍穹支付
 */
@Service
@Slf4j
public class CangqiongPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[苍穹支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.CANGQIONG;
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

            String pay_memberid = normalMchParams.getMchNo();
            String pay_orderid = payOrder.getPayOrderId();


            String pay_bankcode = normalMchParams.getPayType();
            String pay_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String pay_notifyurl = getNotifyUrl(payOrder.getPayOrderId());

            map.put("pay_memberid", pay_memberid);
            map.put("pay_orderid", pay_orderid);

            map.put("pay_bankcode", pay_bankcode);
            map.put("pay_amount", pay_amount);
            map.put("pay_notifyurl", pay_notifyurl);


            String pay_md5sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("pay_md5sign", pay_md5sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
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

    public static void main(String[] args) {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "32cpqp7ui9gmyr0dzviw86g5589nk1mv";

        String pay_memberid = "10000352";
        String pay_orderid = RandomStringUtils.random(15, true, true);

        String pay_bankcode = "999";
        String pay_notifyurl = "http://www.test.com";
        String pay_amount = "100";


        map.put("pay_memberid", pay_memberid);
        map.put("pay_orderid", pay_orderid);
        map.put("pay_bankcode", pay_bankcode);
        map.put("pay_notifyurl", pay_notifyurl);
        map.put("pay_amount", pay_amount);

        String pay_md5sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("pay_md5sign", pay_md5sign);


        String payGateway = "http://pay.zhuanyunpay.top/pay/doPay";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
