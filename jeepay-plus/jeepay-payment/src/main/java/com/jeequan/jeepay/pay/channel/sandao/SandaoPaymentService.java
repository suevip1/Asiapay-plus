package com.jeequan.jeepay.pay.channel.sandao;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.SignatureUtils;
import com.jeequan.jeepay.core.utils.StringKit;
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

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@Slf4j
public class SandaoPaymentService extends AbstractPaymentService {
    private static final String LOG_TAG = "[三道支付]";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SANDAO;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, PayConfigContext payConfigContext) {
        log.info("[{}]开始下单:{}", LOG_TAG, payOrder.getPayOrderId());
        UnifiedOrderRS res = ApiResBuilder.buildSuccess(UnifiedOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);
        String raw = "";
        String rawToken = "";
        try {
            PayPassage payPassage = payConfigContext.getPayPassage();
            //支付参数转换
            SandaoParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), SandaoParams.class);

            SortedMap<String, Object> mapToken = new TreeMap<>();
            String key = normalMchParams.getSecret();
            String tokenStr = RedisUtil.getString("access_token_sandao");
//        String tokenStr = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDA4Mzg0NDAsInVzZXJfbmFtZSI6IntcImlkXCI6MzAzLFwidXNlcm5hbWVcIjpcImxpbWluZ1wiLFwiZ3JvdXBJZHNcIjpbe1wiaWRcIjo0LFwibmFtZVwiOlwicGFydG5lclwiLFwicnVsZUlkc1wiOlt7XCJpZFwiOjQsXCJuYW1lXCI6XCJwYXJ0bmVyXCJ9XX1dfSIsImF1dGhvcml0aWVzIjpbIlJPTEVfcGFydG5lciJdLCJqdGkiOiJSUHplRXVFNXR1Q3hGT2w3a0Z3MDlFOG9PVUkiLCJjbGllbnRfaWQiOiJhcGkiLCJzY29wZSI6WyJhcGkiXX0.gxuPS2F81u8my8u4OWsnh23FcQUmwsPjl8-4MKkA7H6_MEyKmQNxR_N3Dzukf4jg6L9m8sg8b3kptvQi6pHwXosWvwNbJHl-I5i0w2Cd9ngRWLIH9JAWXKQZ_OWzVDJ8OcSb5I5I1CzYsh8RJFpjsuiqbsmhkdPyNPfWTD6H8MYNCR_SzRFO4GD30zRUg-RM-7JADMJNBXRvpGa2uoZQoKcMFVGIjZ10bW-gBvGzKq0KDJU0BjAZrN3z85KUa3kRLBnBAkRyEZk7NNtDBwxFDbRNqVR5c6f-gA9QzYmrsen3jgmwpLWLmmMZZp4bZ2vR6JmaDaxDlJ4xhEIEy9IaQA";
            if (StringUtils.isEmpty(tokenStr)) {
                String tokenUrl = normalMchParams.getTokenUrl();

//        登录用户名::liming  商户ID 303
//        登录密码::#liming2345

                String username = normalMchParams.getUsername();
                String password = normalMchParams.getPassword();
                String grant_type = "api";
                String client_id = "api";
                String client_secret = "knight";

                mapToken.put("username", username);
                mapToken.put("password", password);
                mapToken.put("grant_type", grant_type);
                mapToken.put("client_id", client_id);
                mapToken.put("client_secret", client_secret);

                rawToken = HttpUtil.post(tokenUrl, mapToken, 10000);
                log.info("[{}]请求响应:{}", LOG_TAG, rawToken);
                JSONObject tokenJson = JSONObject.parseObject(rawToken);
                tokenStr = tokenJson.getString("access_token");
                RedisUtil.setString("access_token_sandao", tokenStr, tokenJson.getInteger("expires_in") - 10);

            }


            TreeMap<String, Object> map = new TreeMap<>();
            String payType = normalMchParams.getPayType(); // alipay
            String channel = normalMchParams.getChannel(); // douyin
            String partnerNo = payOrder.getPayOrderId();
            Long amount = payOrder.getAmount();
            Long ts = System.currentTimeMillis();

            map.put("amount", amount);
            map.put("channel", channel);
            map.put("partnerNo", partnerNo);
            map.put("payType", payType);
            map.put("ts", ts);

            TreeMap<String, String> mapData = new TreeMap<>();
            mapData.put("amount", amount + "");
            mapData.put("channel", channel);
            mapData.put("partnerNo", partnerNo);
            mapData.put("payType", payType);
            mapData.put("ts", ts + "");

            String sign = getSign(mapData, null, key);

            String payGateway = normalMchParams.getPayGateway();
            log.info("[{}]请求参数:{}", LOG_TAG, JSONObject.toJSONString(map));

            // 发送POST请求并指定JSON数据
            String payGetUrl = StringKit.appendUrlQuery(payGateway, map);
            log.info("[{}]请求地址:{}", LOG_TAG, payGetUrl);
            HttpResponse response = HttpUtil.createGet(payGetUrl).header("Authorization", "Bearer " + tokenStr).header("Artemis", sign).contentType("application/json,text/plain").timeout(10000).execute();
            // 处理响应
            raw = response.body();

            log.info("[{}]请求响应:{}", LOG_TAG, raw);

            channelRetMsg.setChannelOriginResponse(raw);
            JSONObject result = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (result.getString("code").equals("1")) {

                String payUrl = result.getJSONObject("result").getString("payUrl");
                String passageOrderId = result.getJSONObject("result").getString("orderNo");

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
        String rawToken = "";
        String raw = "";
        SortedMap<String, Object> mapToken = new TreeMap<>();
        String key = "p8lJhAsJkwCWiOaZ";
//        String tokenStr = RedisUtil.getString("access_token_sandao");
        String tokenStr = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDA4NDI5NDksInVzZXJfbmFtZSI6IntcImlkXCI6MzAzLFwidXNlcm5hbWVcIjpcImxpbWluZ1wiLFwiZ3JvdXBJZHNcIjpbe1wiaWRcIjo0LFwibmFtZVwiOlwicGFydG5lclwiLFwicnVsZUlkc1wiOlt7XCJpZFwiOjQsXCJuYW1lXCI6XCJwYXJ0bmVyXCJ9XX1dfSIsImF1dGhvcml0aWVzIjpbIlJPTEVfcGFydG5lciJdLCJqdGkiOiJIdno4MnRIT09XQ0ZvVFFhTTJLaUtieXRORzgiLCJjbGllbnRfaWQiOiJhcGkiLCJzY29wZSI6WyJhcGkiXX0.EinS0Ha870ZlLEARKtib77NV8L86uELaSUSGSC68Z13IrY4tQ-YXDRC4bAosPouHbfB1asjJhuYTqQ_GuMvLP8Z54l4ngbAJnXhyA-kCoJSA4ChNG6F7d_Y8xKgzAKhUBwKwXJX6PFlMF0x6nTEcAFQxJ9rJVDRQj6kpRgUdEA1oAjZMuBxQy4v4NLG1HTA6Rin9_fgUWr4It4M5mlLUS12Hlop5VN79AiOVPE4aZTF7qZCCBuir3tH8JKpio1-DZi6_ei1FgCe_idzzB72QVg82n5sVUx67gxCO1Ll4iIwLGVruW7bmzeNqCXjQnCd1tqqS19XAYNnMG8dCXbnihQ";
        if (StringUtils.isEmpty(tokenStr)) {
            String tokenUrl = "https://sandao999.com/uaa/oauth/token";

//        登录用户名::liming  商户ID 303
//        登录密码::#liming2345

            String username = "liming";
            String password = "#liming2345";
            String grant_type = "api";
            String client_id = "api";
            String client_secret = "knight";

            mapToken.put("username", username);
            mapToken.put("password", password);
            mapToken.put("grant_type", grant_type);
            mapToken.put("client_id", client_id);
            mapToken.put("client_secret", client_secret);

            rawToken = HttpUtil.post(tokenUrl, mapToken, 10000);
            log.info("[{}]请求响应:{}", LOG_TAG, rawToken);
            JSONObject tokenJson = JSONObject.parseObject(rawToken);
            RedisUtil.setString("access_token_sandao", tokenJson.getString("access_token"), tokenJson.getInteger("expires_in") - 10);
        }


        TreeMap<String, Object> map = new TreeMap<>();
        String payType = "alipay";
        String channel = "douyin";
        String partnerNo = RandomStringUtils.random(15, true, true);
        Long amount = 20000L;
        Long ts = System.currentTimeMillis();

        map.put("amount", amount);
        map.put("channel", channel);
        map.put("partnerNo", partnerNo);
        map.put("payType", payType);
        map.put("ts", ts);

        TreeMap<String, String> mapData = new TreeMap<>();
        mapData.put("amount", amount + "");
        mapData.put("channel", channel);
        mapData.put("partnerNo", partnerNo);
        mapData.put("payType", payType);
        mapData.put("ts", ts + "");

        String sign = getSign(mapData, null, key);

        String payGateway = "https://sandao999.com/pay/web/api/pay/order/link";

        // 发送POST请求并指定JSON数据
        String payUrl = StringKit.appendUrlQuery(payGateway, map);
        log.info("[{}]请求地址:{}", LOG_TAG, payUrl);
        HttpResponse response = HttpUtil.createGet(payUrl).header("Authorization", "Bearer " + tokenStr).header("Artemis", sign).contentType("application/json,text/plain").timeout(10000).execute();
        // 处理响应
        raw = response.body();

        log.info("[{}]请求响应:{}", LOG_TAG, raw);
    }

    public static String getSign(TreeMap<String, String> queries, TreeMap<String, String> body, String secret) {
        String text = "";
        if (Objects.nonNull(queries)) {
            String param = queries.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
            text += SignatureUtils.md5(param).toLowerCase();
        } else {
            text += IntStream.range(0, 32).mapToObj(i -> "0").collect(Collectors.joining(""));
        }
        if (Objects.nonNull(body)) {
            String param = JSONObject.toJSONString(body);
            text += SignatureUtils.md5(param).toLowerCase();
        } else {
            text += IntStream.range(0, 32).mapToObj(i -> "0").collect(Collectors.joining(""));
        }
        text = secret + text + secret;
        return SignatureUtils.md5(text).toLowerCase();
    }
}