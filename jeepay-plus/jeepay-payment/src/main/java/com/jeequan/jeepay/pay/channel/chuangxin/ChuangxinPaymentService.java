package com.jeequan.jeepay.pay.channel.chuangxin;

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
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.PayConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class ChuangxinPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[创新支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.CHUANGXIN;
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

            String mchid = normalMchParams.getMchNo();
            String out_trade_no = payOrder.getPayOrderId();
            String amount = AmountUtil.convertCent2Dollar(payOrder.getAmount());
            String channel = normalMchParams.getPayType();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String time_stamp = dateFormat.format(new Date());
            String body = "body";

            map.put("mchid", mchid);
            map.put("out_trade_no", out_trade_no);
            map.put("amount", amount);
            map.put("channel", channel);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            map.put("time_stamp", time_stamp);
            map.put("body", body);
            // 处理用户真实姓名，让商户多传一个字段 extParam
            if (bizRQ != null) {
                if(StringUtils.isNotEmpty(bizRQ.getExtParam())){
                    map.put("body", bizRQ.getExtParam());
                }
            }

            String sign = JeepayKit.getSign(map, key).toLowerCase();
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            raw = HttpUtil.post(payGateway, map, 10000);

            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                JSONObject data = result.getJSONObject("data");
                String payUrl = data.getString("request_url");
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
        String key = "1dce1e88fe7b0885299b5df88f7b3e84";

        String mchid = "100825";
        String channel = "alipayCode";

        String body = "123";


        String out_trade_no = RandomStringUtils.random(15, true, true);
        String amount = AmountUtil.convertCent2Dollar(10000L);


        String notify_url = "https://www.test.com";
        String return_url = notify_url;


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String time_stamp = dateFormat.format(new Date());

        map.put("mchid", mchid);
        map.put("out_trade_no", out_trade_no);

        map.put("channel", channel);
        map.put("amount", amount);

        map.put("body", body);
        map.put("time_stamp", time_stamp);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);
        String sign = JeepayKit.getSign(map, key).toLowerCase();
        map.put("sign", sign);

        String payGateway = "http://webkszf164api.koubofeixingq.com/api/pay/unifiedorder";

        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}