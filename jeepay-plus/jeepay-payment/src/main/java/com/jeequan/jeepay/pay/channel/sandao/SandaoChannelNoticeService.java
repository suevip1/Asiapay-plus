package com.jeequan.jeepay.pay.channel.sandao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.utils.SignatureUtils;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@Service
public class SandaoChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[三道支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "SUCCESS";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SANDAO;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {
        try {
            JSONObject params = getReqParamJSON();
            String payOrderId = params.getString("partnerNo");
            return MutablePair.of(payOrderId, params);
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

            String sign = request.getHeader("Artemis");
            log.info("Artemis " + sign);
            jsonParams.put("sign", sign);
            // 校验支付回调
            boolean verifyResult = verifyParams(jsonParams, payOrder, payPassage);
            // 验证参数失败
            if (!verifyResult) {
                //回调参数有问题得
                throw ResponseException.buildText(ON_FAIL);
            }
            log.info("{}验证支付通知数据及签名通过", LOG_TAG);

            SandaoParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), SandaoParams.class);
            String ts = jsonParams.getString("ts");
            String secret = resultsParam.getSecret();
            final String signStr1 = SignatureUtils.md5(secret) + ts;
            final String signStr = SignatureUtils.md5(signStr1);

            ResponseEntity okResponse = textResp(signStr);
            result.setResponseEntity(okResponse);

            //success	支付成功 cancel	已取消
            String status = jsonParams.getString("status");

            result.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            if (status.equals("cancel")) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, status);
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            }
            if (status.equals("success")) {
                //验签成功后判断上游订单状态
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }
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
        String orderNo = jsonParams.getString("partnerNo");        // 商户订单号
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

        SandaoParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), SandaoParams.class);

        String sign = jsonParams.getString("sign");

        TreeMap<String, String> map = new TreeMap<>();
        String status = jsonParams.getString("status");
        String channel = jsonParams.getString("channel");
        String partnerNo = jsonParams.getString("partnerNo");
        String partnerId = jsonParams.getString("partnerId");
        String amount = jsonParams.getString("amount");
        String ts = jsonParams.getString("ts");

        map.put("amount", amount);
        map.put("channel", channel);
        map.put("partnerNo", partnerNo);
        map.put("partnerId", partnerId);
        map.put("status", status);
        map.put("ts", ts);

        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            final String signStr = getSign(map, null, secret);
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

    public static void main(String[] args) {

        String testStr = "{\"partnerNo\":\"P1763588335234289665\",\"amount\":\"20000\",\"channel\":\"douyin\",\"sign\":\"b6718c11f0544e06ab8ed7165064bdfa\",\"partnerId\":\"303\",\"status\":\"cancel\",\"ts\":\"1709308880827\"}";

        JSONObject jsonParams = JSON.parseObject(testStr);
        TreeMap<String, String> map = new TreeMap<>();

        String status = jsonParams.getString("status");
        String channel = jsonParams.getString("channel");
        String partnerNo = jsonParams.getString("partnerNo");
        String partnerId = jsonParams.getString("partnerId");
        String amount = jsonParams.getString("amount");
        String ts = jsonParams.getString("ts");

        map.put("amount", amount);
        map.put("channel", channel);
        map.put("partnerNo", partnerNo);
        map.put("partnerId", partnerId);
        map.put("status", status);
        map.put("ts", ts);

        String sign = jsonParams.getString("sign");

        String secret = "p8lJhAsJkwCWiOaZ";
        final String signStr = getSign(map, null, secret);
        log.info(signStr);
        log.info(signStr.equalsIgnoreCase(sign) + "");
    }
}