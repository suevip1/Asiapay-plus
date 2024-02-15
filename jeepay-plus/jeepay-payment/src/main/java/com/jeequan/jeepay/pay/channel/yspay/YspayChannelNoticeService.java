package com.jeequan.jeepay.pay.channel.yspay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.SignatureUtils;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.util.JeepayKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;


@Slf4j
@Service
public class YspayChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "YS支付";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "ok";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSPAY;
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

            //支付状态pay_status1 订单已支付
            //2 订单已关闭
            int status = jsonParams.getInteger("pay_status");

            if (status != 1) {
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

        BigDecimal channelNotifyAmount = new BigDecimal(Double.parseDouble(txnAmt) * 100);
        BigDecimal orderAmount = new BigDecimal(payOrder.getAmount());

        NormalMchParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

        String sign = jsonParams.getString("sign");
        Map<String, Object> map = JSON.parseObject(jsonParams.toJSONString());
        String timestamp = map.get("timestamp").toString();
        map.replace("timestamp", URLEncoder.encode(timestamp));
        map.remove("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            String signMapStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&api_secret=" + secret;
            final String signStr = SignatureUtils.md5(signMapStr).toLowerCase();
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
        String secret = "cae1b8ce6bf07efa8af88a1ce998a952";
        String test = "{\"pay_status\":\"1\",\"out_trade_no\":\"P1755949376910503937\",\"appid\":\"cdd5d926dae105593a9bad1c0b42587b\",\"pay_amount\":\"100\",\"sign\":\"c547cd761de7db2c81b1b938ddc23949\",\"trade_no\":\"240209213850637100\",\"timestamp\":\"2024-02-09 21:38:50\"}";

        Map<String, Object> map = JSON.parseObject(test);
        String timestamp = map.get("timestamp").toString();
        map.replace("timestamp", URLEncoder.encode(timestamp));
        map.remove("sign");
        String signMapStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&api_secret=" + secret;
        final String signStr = SignatureUtils.md5(signMapStr).toLowerCase();
        log.info(signStr);
    }

}