package com.jeequan.jeepay.pay.channel.rixinpay3;

import cn.hutool.core.date.DateUtil;
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
public class Rixinpay3PaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[日鑫3支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.RIXINPAY3;
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
            String pay_applydate = DateUtil.now();


            String pay_bankcode = normalMchParams.getPayType();
            String pay_notifyurl = getNotifyUrl(payOrder.getPayOrderId());
            String pay_callbackurl = pay_notifyurl;


            String pay_amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String pay_user_ip = payOrder.getClientIp();


            map.put("pay_memberid", pay_memberid);
            map.put("pay_orderid", pay_orderid);
            map.put("pay_applydate", pay_applydate);
            map.put("pay_bankcode", pay_bankcode);

            map.put("pay_notifyurl", pay_notifyurl);
            map.put("pay_callbackurl", pay_callbackurl);

            map.put("pay_amount", pay_amount);
            String sign = JeepayKit.getSign(map, key).toUpperCase();
            map.put("pay_md5sign", sign);
            map.put("pay_user_ip", pay_user_ip);
            map.put("pay_productname", "下单");

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {

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
        String key = "qq0io7vrgbu7774xfwvq4bolkizmvt1t";

        String pay_memberid = "230869685";
        String pay_orderid = RandomStringUtils.random(15, true, true);
        String pay_applydate = DateUtil.now();


        String pay_bankcode = "106";
        String pay_notifyurl = "https://www.test.com";
        String pay_callbackurl = pay_notifyurl;


        String pay_amount = AmountUtil.convertCent2Dollar(39900L);
        String pay_user_ip = "127.0.0.1";

        log.info("[{}]请求响应:{}", LOG_TAG, pay_applydate);
        map.put("pay_memberid", pay_memberid);
        map.put("pay_orderid", pay_orderid);
        map.put("pay_applydate", pay_applydate);
        map.put("pay_bankcode", pay_bankcode);

        map.put("pay_notifyurl", pay_notifyurl);
        map.put("pay_callbackurl", pay_callbackurl);

        map.put("pay_amount", pay_amount);
        String sign = JeepayKit.getSign(map, key).toUpperCase();
        map.put("pay_md5sign", sign);
        map.put("pay_user_ip", pay_user_ip);
        map.put("pay_productname", "下单");

        String payGateway = "https://zzjijipay.eazyzhi.com/Pay_index.html";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}
