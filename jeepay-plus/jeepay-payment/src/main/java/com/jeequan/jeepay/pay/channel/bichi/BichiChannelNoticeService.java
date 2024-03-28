package com.jeequan.jeepay.pay.channel.bichi;

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


@Slf4j
@Service
public class BichiChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[碧池支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.BICHI;
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

            //支付状态； 1支付中  2支付成功  3支付失败  4异常订单
            int status = jsonParams.getInteger("pay_status");

            if (status != 2) {
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
        String orderNo = jsonParams.getString("out_order_no");        // 商户订单号
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

        BichiParamsModel resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), BichiParamsModel.class);

        String sign = jsonParams.getString("sign");
        String amount = jsonParams.getString("amount");
        String channel = jsonParams.getString("channel");
        String merchant_no = jsonParams.getString("merchant_no");
        String order_no = jsonParams.getString("order_no");
        String out_order_no = jsonParams.getString("out_order_no");
        String pay_status = jsonParams.getString("pay_status");
        String timestamp = jsonParams.getString("timestamp");

        if (resultsParam != null) {
            //sign = md5(amount + channel + merchant_no + order_no + out_order_no + pay_status + timestamp + 商户秘钥);
            String secret = resultsParam.getSecret();
            final String signContentStr = amount + channel + merchant_no + order_no + out_order_no + pay_status + timestamp + secret;
            final String signStr = SignatureUtils.md5(signContentStr).toLowerCase();
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
        String str = "{\"order_no\":\"7351333636208477817\",\"merchant_no\":10006,\"amount\":\"100.00\",\"pay_status\":1,\"channel\":10001,\"sign\":\"9e4778105f7e23b7bc2c6255f4fd9086\",\"out_order_no\":\"P1773271055371726850\",\"timestamp\":1711615781}";

        JSONObject jsonParams = JSONObject.parseObject(str);
        String sign = jsonParams.getString("sign");
        log.info("回调sign：{}", sign);

        String amount = jsonParams.getString("amount");
        String channel = jsonParams.getString("channel");
        String merchant_no = jsonParams.getString("merchant_no");
        String order_no = jsonParams.getString("order_no");
        String out_order_no = jsonParams.getString("out_order_no");
        String pay_status = jsonParams.getString("pay_status");
        String timestamp = jsonParams.getString("timestamp");

        String secret = "f9c5156f539b933f7c529ce6a7d41abaeefbbeaa";

        final String signContentStr = amount + channel + merchant_no + order_no + out_order_no + pay_status + timestamp + secret;
        final String signStr = SignatureUtils.md5(signContentStr).toLowerCase();
        log.info("验签sign：{}", signStr);
    }
}