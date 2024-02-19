package com.jeequan.jeepay.pay.channel.jqkpay;


import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JqkpayChannelNoticeService extends AbstractChannelNoticeService {
    private static final String LOG_TAG = "[jqk支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.JQKPAY;
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


            ///查单
            NormalMchParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);
            String key = normalMchParams.getSecret();

            Map<String, Object> map = new HashMap<>();
            String mchid = normalMchParams.getMchNo();
            String out_trade_no = payOrder.getPayOrderId();
            String timestamp = System.currentTimeMillis() / 1000 + "";
            map.put("mchid", mchid);
            map.put("out_trade_no", out_trade_no);
            map.put("timestamp", timestamp);
            String sign = JeepayKit.getSign(map, key);
            map.put("sign", sign);

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(normalMchParams.getQueryUrl()).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            String raw = response.body();
            log.info("{} 查单请求响应:{}", LOG_TAG, raw);
            JSONObject queryResult = JSON.parseObject(raw, JSONObject.class);

            //支付状态：NOTPAY：未支付；SUCCESS：已支付
            String status = queryResult.getJSONObject("data").getString("status");

            if (!status.equals("SUCCESS")) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, status);
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            } else {
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
        String orderNo = jsonParams.getString("out_trade_no");        // 商户订单号
        String txnAmt = jsonParams.getString("pay_amount");        // 支付金额

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

        NormalMchParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

        String sign = jsonParams.getString("sign");
        Map<String, Object> map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            final String signContentStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + secret;
            final String signStr = SignatureUtils.md5(signContentStr).toUpperCase();
            if (signStr.equalsIgnoreCase(sign)) {
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
        String test = "{\n" +
                "    \"order_no\": \"20240219010551877558\",\n" +
                "    \"mchid\": 10642,\n" +
                "    \"out_trade_no\": \"P1759262967347044354\",\n" +
                "    \"pay_amount\": \"1.01\",\n" +
                "    \"sign\": \"06B83273B32A012BCA86516D5DB9302C\",\n" +
                "    \"attach\": \"\",\n" +
                "    \"success_time\": 1708275966,\n" +
                "    \"status\": \"SUCCESS\"\n" +
                "}";

        Map map = JSON.parseObject(test);
        String secret = "W80msPjEwY9j5Hj1YxI2bMRg8ig1vOfp";
        map.remove("sign");
        final String signContentStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + secret;
        log.info(signContentStr);
        final String signStr = SignatureUtils.md5(signContentStr).toUpperCase();
        log.info(signStr);

    }
}
