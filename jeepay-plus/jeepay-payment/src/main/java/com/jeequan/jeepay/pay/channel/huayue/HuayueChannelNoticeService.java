package com.jeequan.jeepay.pay.channel.huayue;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.utils.SignatureUtils;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.util.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class HuayueChannelNoticeService extends AbstractChannelNoticeService {
    private static final String LOG_TAG = "[华悦支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HUAYUE;
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
            //按格式排序
            String fromatParams = formatJson(params.toString());
            // 获取请求参数【加上Feature.OrderedField参数，防止数据乱序】
            JSONObject jsonParams = JSONObject.parseObject(fromatParams, Feature.OrderedField);
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
            JSONObject paramsJson = jsonParams.getJSONObject("paramsJson");
            String orderId = paramsJson.getJSONObject("data").getString("orderId");        // 商户订单号
            HuayueParamsModel huayueParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), HuayueParamsModel.class);
            map.put("partner", huayueParamsModel.getMchNo());
            map.put("orderId", orderId);
            String sign = SignatureUtils.md5(huayueParamsModel.getMchNo() + huayueParamsModel.getSecret() + orderId).toLowerCase();
            map.put("sign", sign);
            map.put("isLoop", "no");

            String raw = HttpUtil.get(huayueParamsModel.getQueryUrl(), map, 10000);
            log.info("{} 查单请求响应:{}", LOG_TAG, raw);
            JSONObject queryResult = JSON.parseObject(raw, JSONObject.class);

            //只有请求code为000000，status为SUCCESS，表示订单支付成功
            if (queryResult.getString("code").equals("000000")) {
                String status = queryResult.getJSONObject("data").getString("status");
                if (status.equals("SUCCESS")) {
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
        JSONObject paramsJson = jsonParams.getJSONObject("paramsJson");
        String orderNo = paramsJson.getJSONObject("data").getString("orderId");        // 商户订单号
        String txnAmt = paramsJson.getJSONObject("data").getString("orderAmount");        // 支付金额

        if (StringUtils.isEmpty(orderNo)) {
            log.info("订单ID为空 [orderNo]={}", orderNo);
            return false;
        }
        if (StringUtils.isEmpty(txnAmt)) {
            log.info("金额参数为空 [txnAmt] :{}", txnAmt);
            return false;
        }

        BigDecimal channelNotifyAmount = new BigDecimal(txnAmt);
        BigDecimal orderAmount = BigDecimalUtil.INSTANCE.divide(payOrder.getAmount(), 100f);

        HuayueParamsModel resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), HuayueParamsModel.class);

        String sign = jsonParams.getString("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();

            //1.取出需要验签的字符串
            String paramsJsonStr = String.valueOf(paramsJson);
            //2.将paramsJsonStr 字段base64编码
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] base64 = encoder.encode(paramsJsonStr.getBytes());
            String base64Str = new String(base64);
            //3.将经过base64编码的paramsJson串MD5加密
            String base64Md5 = SignatureUtils.md5(base64Str).toLowerCase();
            //4.密匙拼接base64Md5字符串,MD5加密后转大写
            String signStr = (SignatureUtils.md5(secret + base64Md5)).toUpperCase();
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

    /**
     * JSON数据格式转换
     * @param json
     * @return
     * @throws JsonProcessingException
     */
    private static String formatJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.readValue(json, ObjectNode.class);

        // 重新排序
        ObjectNode paramsJson = (ObjectNode) root.remove("paramsJson");
        paramsJson.put("message", paramsJson.remove("message"));

        ObjectNode data = (ObjectNode) paramsJson.remove("data");
        data.put("orderId", data.remove("orderId"));
        data.put("tradeNo", data.remove("tradeNo"));
        data.put("outTradeNo", data.remove("outTradeNo"));
        data.put("orderAmount", data.remove("orderAmount"));
        data.put("payAmount", data.remove("payAmount"));
        data.put("dateTime", data.remove("dateTime"));

        paramsJson.set("data", data);
        root.set("paramsJson", paramsJson);

        String formattedJson = mapper.writeValueAsString(root);
        return formattedJson;
    }


    public static void main(String[] args) throws JsonProcessingException {
        String body = "{\"paramsJson\":{\"code\":\"000000\",\"data\":{\"dateTime\":\"2023-11-23 19:17:58\",\"orderAmount\":1,\"payAmount\":1,\"tradeNo\":\"budan\",\"orderId\":\"P1727644295292850177\",\"outTradeNo\":\"012717007374735900491\"},\"message\":\"success\"},\"sign\":\"DEE89636C867AF1418290D142EA71544\"}";
        body = formatJson(body);

        //加上Feature.OrderedField参数，防止数据乱序
        JSONObject jsonParams = JSONObject.parseObject(body, Feature.OrderedField);
        String orderNo = jsonParams.getJSONObject("paramsJson").getJSONObject("data").getString("orderId");        // 商户订单号
        System.out.println(orderNo);

        String paramsJsonStr = String.valueOf(jsonParams.get("paramsJson"));
        System.out.println(paramsJsonStr);
        //将paramsJsonStr 字段base64编码
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] base64 = encoder.encode(paramsJsonStr.getBytes());
        String base64Str = new String(base64);
        System.out.println(base64Str);
        //将经过base64编码的paramsJson串MD5加密
        String base64Md5 = SignatureUtils.md5(base64Str).toLowerCase();
        System.out.println(base64Md5);
        //密匙拼接base64Md5字符串,MD5加密后转大写
        System.out.println("CADDC62088AC1C77DB4C198F22AD3662" + base64Md5);
        String vSign = (SignatureUtils.md5("CADDC62088AC1C77DB4C198F22AD3662" + base64Md5)).toUpperCase();
        System.out.println(vSign);
    }
}
