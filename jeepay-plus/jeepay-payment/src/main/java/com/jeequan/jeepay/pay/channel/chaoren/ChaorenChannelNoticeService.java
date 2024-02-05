package com.jeequan.jeepay.pay.channel.chaoren;

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
import java.util.Map;


@Slf4j
@Service
public class ChaorenChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[超人支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "ok";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.CHAOREN;
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

            // 待匹配|待支付|已支付|匹配失败|金额错误|超时关闭
            String payStatus = jsonParams.getJSONObject("data").getString("payStatus");
            if (!payStatus.equals("已支付")) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, payStatus);
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
        String orderNo = jsonParams.getJSONObject("data").getString("out_trade_no");        // 商户订单号
        String txnAmt = jsonParams.getJSONObject("data").getString("payAmount");        // 支付金额

        if (StringUtils.isEmpty(orderNo)) {
            log.info("订单ID为空 [orderNo]={}", orderNo);
            return false;
        }
        if (StringUtils.isEmpty(txnAmt)) {
            log.info("金额参数为空 [txnAmt] :{}", txnAmt);
            return false;
        }

        ChaoRenParamsModel chaoRenParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), ChaoRenParamsModel.class);

        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.getJSONObject("data").toJSONString());
        map.remove("sign");
        if (chaoRenParamsModel != null) {
            String secret = chaoRenParamsModel.getSecret().replaceAll("\\s+", "");
            final String signContentStr = orderNo + map.get("payStatus").toString() + map.get("mobile").toString() + txnAmt;
            boolean signResult = SignatureUtils.buildRSASHA1VerifyByPublicKey(signContentStr, sign, secret);
            if (signResult) {
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
        String str = "{\"data\":{\"orderId\":\"96ee2957630941dea350c3348dea4cf4\",\"out_trade_no\":\"88888888\",\"payStatus\":\"已支付\",\"payTime\":\"2022-06-25 13:41:52\",\"mobile\":\"13888888888\",\"payAmount\":\"100.00\",\"errorAmount\":null,\"phoneBalance\":null,\"afterPhoneBalance\":null,\"tradeNO\":\"1000698043424\"},\"sign\":\"AD272A3EAD59C4C1ADD58AD6DC4D68EA\"}\n";

        JSONObject jsonParams = JSONObject.parseObject(str);
        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.getJSONObject("data").toJSONString());
        map.remove("Sign");
        String secret = "zPK5KwymlMCvJ3J1g1NMINlBmMdNvKfKVwKlyoA7";
        String signContent = map.get("out_trade_no").toString() + map.get("payStatus").toString() + map.get("mobile").toString() + map.get("payAmount").toString();
        boolean signResult = SignatureUtils.buildRSASHA1VerifyByPublicKey(signContent, sign, secret);
        log.info("校验结果：{}", signResult);
    }
}