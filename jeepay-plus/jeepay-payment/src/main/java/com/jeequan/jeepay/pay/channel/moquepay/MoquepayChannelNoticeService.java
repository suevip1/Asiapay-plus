package com.jeequan.jeepay.pay.channel.moquepay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
        String txnAmt = jsonParams.getString("actualAmount");        // 支付金额

        if (StringUtils.isEmpty(orderNo)) {
            log.info("订单ID为空 [orderNo]={}", orderNo);
            return false;
        }
        if (StringUtils.isEmpty(txnAmt)) {
            log.info("金额参数为空 [txnAmt] :{}", txnAmt);
            return false;
        }

        BigDecimal channelNotifyAmount = new BigDecimal(Double.parseDouble(txnAmt) * 100);
        BigDecimal orderAmount = new BigDecimal(payOrder.getAmount());

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
        String test = "{\n" +
                "    \"receiptAmount\": \"100.00\",\n" +
                "    \"amount\": \"100.00\",\n" +
                "    \"notifyType\": \"post\",\n" +
                "    \"orderNo\": \"I1753065245018652672\",\n" +
                "    \"merchantId\": \"rbQbqbZF1-118\",\n" +
                "    \"orderId\": \"P1753065246771736578\",\n" +
                "    \"payTime\": \"1706798332000\",\n" +
                "    \"retryCount\": \"5\",\n" +
                "    \"sign\": \"aea600255b17564543c152b3cec28746\",\n" +
                "    \"notifyUrl\": \"http://pay-api.klnkln-pay.net/api/pay/notify/mackpay/P1753065246771736578\",\n" +
                "    \"orderStatus\": \"1\",\n" +
                "    \"attach\": \"attach\"\n" +
                "}";

        Map map = JSON.parseObject(test);

        String key = "485be16ef429444bad80e09564fb3dfb";
        final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign", "merchantId", "notifyUrl", "notifyType", "retryCount"}) + "&key=" + key;
        final String sign = SignatureUtils.md5(signContentStr).toLowerCase();
        log.info(sign);
    }
}