package com.jeequan.jeepay.pay.channel.liyupay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.HttpClientPoolUtil;
import com.jeequan.jeepay.core.utils.SignatureUtils;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.channel.rongfu.RongFuParamsModel;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LiyupayChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[鲤鱼支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.LIYUPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {
        try {
            JSONObject params = getReqParamJSON();
            return MutablePair.of(urlOrderId, params);
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, PayPassage payPassage, NoticeTypeEnum noticeTypeEnum) {
        ChannelRetMsg result = ChannelRetMsg.confirmSuccess(null);
        try {
            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            log.info("{} 回调参数, jsonParams：{}", LOG_TAG, jsonParams);

            // 校验支付回调
            boolean verifyResult = verifyParams(jsonParams, payOrder, payPassage);
            // 验证参数失败
            if (!verifyResult) {
                //回调参数有问题得
                throw ResponseException.buildText(ON_FAIL);
            }
            log.info("{}验证支付通知数据及签名通过", LOG_TAG);

            ResponseEntity okResponse = textResp(ON_SUCCESS);
            result.setResponseEntity(okResponse);

            //查单
            Map<String, Object> map = new HashMap<>();
            RongFuParamsModel normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), RongFuParamsModel.class);
            map.put("orderNo", jsonParams.getString("orderNo"));
            map.put("merchantId", normalMchParams.getMchNo());
            map.put("type", normalMchParams.getAppId());

            String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&secret=" + normalMchParams.getSecret();
            String sign = SignatureUtils.md5(SignStr).toUpperCase();


            Map<String, Object> mapData = new HashMap<>();
            mapData.put("data", map);
            mapData.put("sign", sign);

            log.info("{} 查单请求:{}", LOG_TAG, JSONObject.toJSONString(mapData));
            String raw = HttpClientPoolUtil.doPost(normalMchParams.getQueryUrl(),mapData);
            log.info("{} 查单请求响应:{}", LOG_TAG, raw);
            JSONObject queryResult = JSON.parseObject(raw, JSONObject.class);

            if (queryResult.getString("code").equals("0")) {
                JSONObject data = queryResult.getJSONObject("data");

                //支付状态：付款成功-200
                String payStatus = data.getString("bindState");
                if (payStatus.equals("200")) {
                    //验签成功后判断上游订单状态
                    result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    return result;
                }
            }
            log.info("{}回调通知订单状态错误:{}", LOG_TAG, raw);
            result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText(ON_FAIL);
        }
    }

    /**
     * 校验签名及金额
     *
     * @param jsonParams
     * @param payOrder
     * @param payPassage
     * @return
     */
    public boolean verifyParams(JSONObject jsonParams, PayOrder payOrder, PayPassage payPassage) {
        String orderNo = jsonParams.getString("merchantOrderNo");        // 商户订单号
        String txnAmt = jsonParams.getString("amount");        // 支付金额

        if (StringUtils.isEmpty(orderNo)) {
            log.info("订单ID为空 [orderNo]={}", orderNo);
            return false;
        }
        if (StringUtils.isEmpty(txnAmt)) {
            log.info("金额参数为空 [txnAmt] :{}", txnAmt);
            return false;
        }

        BigDecimal channelNotifyAmount = new BigDecimal(txnAmt);
        BigDecimal orderAmount = new BigDecimal(payOrder.getAmount());

        NormalMchParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();

            List<String> fieldOrder = Arrays.asList("merchantId", "merchantOrderNo", "orderNo", "amount", "secret");
            StringBuilder sb = new StringBuilder();
            for (String field : fieldOrder) {
                if (map.containsKey(field)) {
                    Object value = map.get(field);
                    sb.append(field).append("=").append(value).append("&");
                }
            }
            sb.append("secret=").append(secret);
            final String signContent = sb.toString();
            final String signStr = SignatureUtils.md5(signContent).toLowerCase();
            if (signStr.equalsIgnoreCase(sign) && orderAmount.compareTo(channelNotifyAmount) == 0) {
                return true;
            } else {
                log.error("{} 验签或校验金额失败！ 回调参数：parameter = {}", LOG_TAG, jsonParams);
                return false;
            }
        } else {
            log.info("{} 获取商户配置失败！ 参数：parameter = {}", LOG_TAG, jsonParams);
            return false;
        }
    }

    public static void main(String[] args) {
//        String jsonParams = "{\"data\":{\"merchantId\":\"8261558\",\"merchantOrderNo\":\"P1757675615797399553\"},\"sign\":\"a151a750b560d04df2755c4ba4106b91\"}";
//        Map map = JSON.parseObject(jsonParams);
//        String sign = (String) map.get("sign");
//        System.out.println(sign);
//        map.remove("sign");
//
//        String secret = "0f0570ce6423e17c6108c049d19649bc";
//        List<String> fieldOrder = Arrays.asList("merchantId", "merchantOrderNo", "orderNo", "amount", "secret");
//        StringBuilder sb = new StringBuilder();
//        for (String field : fieldOrder) {
//            if (map.containsKey(field)) {
//                Object value = map.get(field);
//                sb.append(field).append("=").append(value).append("&");
//            }
//        }
//        sb.append("secret=").append(secret);
//
//        String signContent = sb.toString();
//        System.out.println(signContent);
//        final String signStr = SignatureUtils.md5(signContent).toLowerCase();
//        System.out.println(signStr);

//        String test = "{"data":{"merchantId":"8261558","type":"1","merchantOrderNo":"P1757715565003698177"},"sign":"b6bfd65417fc204d77a04234ee18f885"}";
        Map<String, Object> map = new HashMap<>();
        map.put("merchantId", "8261558");
        map.put("orderNo", "2402141558171017731");
        map.put("type", "1");

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("data", map);
        String key = "96b3c7fbb1d37e6b6c5230df787d166a";

        String SignStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&secret=" + key;
        log.info("签名串:" + SignStr);
        String sign = SignatureUtils.md5(SignStr).toUpperCase();
        mapData.put("sign", sign.toUpperCase());

        log.info("{} 查单请求:{}", LOG_TAG, JSONObject.toJSONString(mapData));

        String raw = HttpClientPoolUtil.doPost("http://47.102.43.27:8011/api/merchant/order/info",mapData);
        log.info("{} 查单请求响应:{}", LOG_TAG, raw);
    }
}