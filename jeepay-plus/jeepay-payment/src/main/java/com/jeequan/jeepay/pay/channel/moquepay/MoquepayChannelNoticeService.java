package com.jeequan.jeepay.pay.channel.moquepay;

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
public class MoquepayChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[MQ支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "SUCCESS";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.MOQUEPAY;
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


            NormalMchParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

            Map<String, Object> mapQuery = new HashMap<>();
            String key = normalMchParams.getSecret();

            String customerId = normalMchParams.getMchNo();
            String orderId = payOrder.getPayOrderId();
            String timestamp = System.currentTimeMillis() / 1000 + "";

            mapQuery.put("customerId", customerId);
            mapQuery.put("orderId", orderId);
            mapQuery.put("timestamp", timestamp);

            String sign = JeepayKit.getSign(mapQuery, key).toUpperCase();
            mapQuery.put("sign", sign);

            String gateway = normalMchParams.getQueryUrl();

            String raw = HttpUtil.post(gateway, mapQuery, 10000);

            //	0验卡中，1处理中，2处理成功，3处理失败
            String status = JSONObject.parseObject(raw).getJSONObject("data").getString("status");

            if (!status.equals("2")) {
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
        String orderNo = jsonParams.getString("orderId");        // 商户订单号
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
        BigDecimal orderAmount = BigDecimalUtil.INSTANCE.divide(payOrder.getAmount(), 100f);

        NormalMchParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();

            final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign"}) + "&key=" + secret;
            final String signStr = SignatureUtils.md5(signContentStr).toUpperCase();
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
        String str = "{\"amount\":\"10\",\"orderId\":\"P1763580433842954241\",\"cardPassword\":\"1a3f5bee3db03f848ecde8803c57b8e054607db1eaca0a4a18e20ed934a51653\",\"systemOrderId\":\"F17093053160231602536\",\"actualAmount\":\"9.0000\",\"sign\":\"534ba894c53a657891bd608597909676\",\"successAmount\":\"9.0000\",\"message\":\"处理成功\",\"extendParams\":\"\",\"successTime\":\"1709305340\",\"customerId\":\"7\",\"cardNumber\":\"51f89dfd1e43c5c352fdb6351e90480354607db1eaca0a4a18e20ed934a51653\",\"status\":\"2\",\"realPrice\":\"9.0000\"}";

        JSONObject jsonParams = JSONObject.parseObject(str);
        String sign = jsonParams.getString("sign");
        log.info("回调sign："+sign);
        Map map = JSON.parseObject(str);
        map.remove("sign");

        String secret = "7XMyhk4gaK6XMkFnw2kB5eA0pHnozY";
        String signContent = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + secret;
        String signStr = SignatureUtils.md5(signContent).toLowerCase();
        log.info(signStr);
    }
}