package com.jeequan.jeepay.pay.channel.tangbang;

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


@Slf4j
@Service
public class TangbangChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[TB支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.TANGBANG;
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

            // 解密回调参数
            TangBangParamsModel tangBangParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), TangBangParamsModel.class);
            if (tangBangParamsModel == null) {
                log.info("{} 获取商户配置失败！ 参数：parameter = {}", LOG_TAG, jsonParams);
            }
            String encryptData = jsonParams.getString("data");
            String decryptedMessage = SignatureUtils.decryptAES(encryptData, tangBangParamsModel.getAesSecret(), null);
            log.info("解密后的内容: " + decryptedMessage);
            JSONObject decryptResult = JSON.parseObject(decryptedMessage);

            // 校验支付回调
            boolean verifyResult = verifyParams(decryptResult, payOrder, payPassage);
            // 验证参数失败
            if (!verifyResult) {
                //回调参数有问题得
                throw ResponseException.buildText(ON_FAIL);
            }
            log.info("{}验证支付通知数据及签名通过", LOG_TAG);

            ResponseEntity okResponse = textResp(ON_SUCCESS);
            result.setResponseEntity(okResponse);

            //订单状态：0-未支付 1-支付成功 4-订单超时
            String status = decryptResult.getString("status");

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
        String orderNo = jsonParams.getString("downstreamNo");        // 商户订单号
        String txnAmt = jsonParams.getString("realMoney");        // 支付金额

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
        if (orderAmount.compareTo(channelNotifyAmount) == 0) {
            return true;
        } else {
            log.error("{} 校验金额失败！ 回调参数：parameter = {}", LOG_TAG, jsonParams);
            return false;
        }
    }

    public static void main(String[] args) {
        String str = "{\"data\":\"a8rJmtZ7fz32yPTn/30otRx8rVhZGQRXKnpRj4NsLb/o0iLgxo3Z65fTJsQuPkbqvkOdL1AyzbEaIYrXu+M4H9C8joVa2G92GLILuiiLC6gKMxIgsHmgktainel3UsCg3W7Jv4aE2NHGtrBgN+evs4/HtDXGDLjxv639iuqdDOyXKpMrn3XbyMYBa0Qxe00lkQS3MEp0RqLkfKJJcmj82xRLelAdNpj0sDP5AelMmjnY6eYHEkCP+NA9+8TVcOvJb9J8+4+inCZJLBtCGxpI7kl9l9NzWZu4xzWzWoxfV9U=\",\"apiCode\":\"35f53d0b43\",\"downstreamNo\":\"P1764702264444403713\"}";
        String secret = "Arimjw8rOUZ85SQU";
        JSONObject jsonParams = JSONObject.parseObject(str);
        String data = jsonParams.getString("data");
        String signStr = SignatureUtils.decryptAES(data, secret, null);
        log.info(signStr);
    }
}