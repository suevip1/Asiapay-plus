package com.jeequan.jeepay.pay.channel.yspay;

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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class YspayPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "YS支付";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSPAY;
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

            String appid = normalMchParams.getMchNo();
            String type = normalMchParams.getPayType();
            String out_trade_no = payOrder.getPayOrderId();
            String pay_amount = AmountUtil.convertCent2DollarShort(payOrder.getAmount());
            String notify_url = URLEncoder.encode(getNotifyUrl(payOrder.getPayOrderId()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = URLEncoder.encode(dateFormat.format(new Date()));

            map.put("appid", appid);
            map.put("type", type);
            map.put("out_trade_no", out_trade_no);
            map.put("pay_amount", pay_amount);
            map.put("notify_url", notify_url);
            map.put("timestamp", timestamp);
            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&api_secret=" + key;
            String sign = SignatureUtils.md5(signStr).toLowerCase();

            map.put("notify_url", URLDecoder.decode(notify_url));
            map.put("timestamp", URLDecoder.decode(timestamp));
            map.put("sign", sign);

            String payGateway = normalMchParams.getPayGateway();

            // 发送POST请求并指定JSON数据
            raw = HttpUtil.post(payGateway, map, 10000);
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("0")) {
                String payUrl = result.getJSONObject("data").getString("pay_url");
                String passageOrderId = result.getJSONObject("data").getString("trade_no");

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
        String key = "cae1b8ce6bf07efa8af88a1ce998a952";

        String appid = "cdd5d926dae105593a9bad1c0b42587b";
        String type = "1";
        String out_trade_no = RandomStringUtils.random(15, false, true);
        String pay_amount = AmountUtil.convertCent2DollarShort(10000L);
        String notify_url = URLEncoder.encode("https://www.test.com");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = URLEncoder.encode(dateFormat.format(new Date()));

        map.put("appid", appid);
        map.put("type", type);
        map.put("out_trade_no", out_trade_no);
        map.put("pay_amount", pay_amount);
        map.put("notify_url", notify_url);
        map.put("timestamp", timestamp);
        String signStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&api_secret=" + key;
        log.error(signStr);
        String sign = SignatureUtils.md5(signStr).toLowerCase();

        map.put("notify_url", URLDecoder.decode(notify_url));
        map.put("timestamp", URLDecoder.decode(timestamp));
        map.put("sign", sign);

        String payGateway = "http://8.217.58.124/api/pay/gateway";

        // 发送POST请求并指定JSON数据
//        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").execute();
//        // 处理响应
//        raw = response.body();
        log.info(JSONObject.toJSONString(map));
        raw = HttpUtil.post(payGateway, map, 10000);
        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

}