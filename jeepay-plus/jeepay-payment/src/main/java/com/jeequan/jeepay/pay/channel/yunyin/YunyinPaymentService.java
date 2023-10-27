package com.jeequan.jeepay.pay.channel.yunyin;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.*;
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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 云银支付
 */
@Service
@Slf4j
public class YunyinPaymentService extends AbstractPaymentService {

    private static final String LOG_TAG = "[云银支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YUNYIN;
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
            YunyinParamsModel yunyinParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), YunyinParamsModel.class);

            Map<String, Object> map = new HashMap<>();
            String key = yunyinParamsModel.getSecret();

            String mch_id = yunyinParamsModel.getMchNo();
            String type = yunyinParamsModel.getPayType();
            String out_trade_no = payOrder.getPayOrderId();
            String notify_url = getNotifyUrl(payOrder.getPayOrderId());
            String return_url = notify_url;
            String subject = "subject";
            BigDecimal amount = new BigDecimal(AmountUtil.convertCent2Dollar(payOrder.getAmount()));
            String ip = payOrder.getClientIp();

            map.put("mch_id", mch_id);
            map.put("type", type);
            map.put("out_trade_no", out_trade_no);
            map.put("notify_url", notify_url);
            map.put("return_url", return_url);
            map.put("subject", subject);
            map.put("amount", amount);
            map.put("ip", ip);

            String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null);
            SignStr += key;
            String sign = SignatureUtils.md5(SignStr).toLowerCase();
            map.put("sign", sign);
            map.put("sign_type", "MD5");

            String payGateway = yunyinParamsModel.getPayGateway();

            String contentStr = SignatureUtils.getSignContentValEncode(map, null ,null);

            // 指定请求体的Content-Type为JSON
            HttpResponse response = HttpUtil.createPost(payGateway).header("reserve", yunyinParamsModel.getReserve()).body(JSONObject.toJSON(contentStr).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            raw = response.body();
            channelRetMsg.setChannelOriginResponse(raw);
            log.info("[{}]请求响应:{}", LOG_TAG, raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("200")) {
                JSONObject data = result.getJSONObject("result");
                String payUrl = "";
                if(type.equals("1")){
                    payUrl = data.getString("pay_url");
                }else{
                    payUrl = data.getString("qr_code");
                }

                res.setPayDataType(CS.PAY_DATA_TYPE.PAY_URL);
                res.setPayData(payUrl);

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

    public static void main(String[] args) throws UnsupportedEncodingException {
        String raw = "";

        Map<String, Object> map = new HashMap<>();
        String key = "ZYUuH6TcIRGhT8U8ZgnVWBNHseHozYAK";

        String mch_id = "1394";
        String type = "1";
        String out_trade_no = RandomStringUtils.random(15, true, true);
        String notify_url = "https://www.test.com";
        String return_url = notify_url;
        String subject = "subject";
        String amount = AmountUtil.convertCent2Dollar(10000L);
        String ip = "127.0.0.1";
        String sign_type = "MD5";

        map.put("mch_id", mch_id);
        map.put("type", type);
        map.put("out_trade_no", out_trade_no);
        map.put("notify_url", notify_url);
        map.put("return_url", return_url);
        map.put("subject", subject);
        map.put("amount", amount);
        map.put("ip", ip);

        String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null);
        SignStr += key;
        String sign = SignatureUtils.md5(SignStr).toLowerCase();
        log.info("[{}]签名:{}", LOG_TAG, SignatureUtils.md5(SignStr).toLowerCase());

        map.put("sign", sign);
        map.put("sign_type", sign_type);
        log.info("[{}]请求Map:{}", LOG_TAG, JSONObject.toJSON(map));

        String payGateway = "https://yyapi.yunyinpay.com:9500/ThirdApi/Pay/create_order";

        String contentStr = SignatureUtils.getSignContentValEncode(map, null ,null);
        log.info("[{}]请求参数:{}", LOG_TAG, contentStr);

        // 指定请求体的Content-Type为JSON
        HttpResponse response = HttpUtil.createPost(payGateway).header("reserve", "478814").body(JSONObject.toJSON(contentStr).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        raw = response.body();

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }
}