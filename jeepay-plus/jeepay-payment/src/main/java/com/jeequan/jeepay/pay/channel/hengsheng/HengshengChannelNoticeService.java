package com.jeequan.jeepay.pay.channel.hengsheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.Map;


@Slf4j
@Service
public class HengshengChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[恒生支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HENGSHENG;
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

            //订单支付状态 0：处理中 1：成功 2：失败
            String status = jsonParams.getString("status");

            if (!status.equals("1")) {
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
        BigDecimal orderAmount = BigDecimalUtil.INSTANCE.divide(payOrder.getAmount(), 100f);

        HengshengParamsModel hengshengParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), HengshengParamsModel.class);

        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (hengshengParamsModel != null) {
            String requestPublicKey = hengshengParamsModel.getRequestPublicKey();
            final String signContentStr = SignatureUtils.getSignContent(map, null, new String[]{""});
            boolean verifySign = SignatureUtils.buildSHA1WithRSAVerifyByPublicKey(signContentStr, requestPublicKey, sign);
            if (verifySign && orderAmount.compareTo(channelNotifyAmount) == 0) {
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

    public static void main(String[] args){
        String jsonParams = "{\"amount\":\"799.98\",\"merchantId\":\"80047\",\"sign\":\"GOfeO8pfRtdoEF+HpUuXV63KqbREhG328NfiMFtDzjMWwmFGqjRn9KtCjQQcHopARU9mHShAeNjvBdVJdz09WqZJHlGqrA3OAmPiT7+hUUAXeoKoTPzTvbxLOC2A/2nVTkdV1XHdxntiE/NOunnRK77vlgczcHZ9kcPx0eUGOVI=\",\"merchantOrderNo\":\"P1724709334316826625\",\"status\":\"1\"}";
        Map map = JSON.parseObject(jsonParams);
        String sign = map.get("sign").toString();
        System.out.println("返回Sign："+ sign);
        map.remove("sign");
        String requestPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQqnp6PMs5X2MTwuGqYV1j6rKmyMZeZcgycBDlSJS1gob2BePAkJBgYl9b0ks2HFmuPc9wSK5He1M5q2le/u02ahUsdaL2e0uOXK/fwpADS5DpbICXoUDG9A1pag5TFjJFt/xVRUkkB8mWnnHXwbAQfrjazuuzvXjPW/4Q7M8J5wIDAQAB";
        final String signContentStr = SignatureUtils.getSignContent(map, null, new String[]{""});
        boolean verifySign = SignatureUtils.buildSHA1WithRSAVerifyByPublicKey(signContentStr, requestPublicKey, sign);
        System.out.println(verifySign);
        String txnAmt = "799.98";
        BigDecimal channelNotifyAmount = new BigDecimal(txnAmt);
        Long amount = 79998L;
        BigDecimal orderAmount = BigDecimalUtil.INSTANCE.divide(amount, 100f);
//        BigDecimal orderAmount = new BigDecimal(amount / 100f);
        if (orderAmount.compareTo(channelNotifyAmount) == 0) {
            System.out.println("校验金额成功");
        }else{
            System.out.println("校验金额失败");
        }
    }
}