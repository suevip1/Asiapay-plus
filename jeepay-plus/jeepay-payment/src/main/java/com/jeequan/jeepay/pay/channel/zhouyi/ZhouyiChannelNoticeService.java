package com.jeequan.jeepay.pay.channel.zhouyi;

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
public class ZhouyiChannelNoticeService extends AbstractChannelNoticeService {
    private static final String LOG_TAG = "[周易支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ZHOUYI;
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

            //1为成功 订单回调通知（POST）只通知（成功的订单）
            String status = jsonParams.getString("code");
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

        JSONObject jsonRespData = jsonParams.getJSONObject("data");

        String orderNo = jsonRespData.getString("out_order_no");        // 商户订单号
        String txnAmt = jsonRespData.getString("amount");        // 支付金额

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


        Map map = JSON.parseObject(jsonParams.getJSONObject("data").toJSONString());
        String sign = map.get("sign").toString();

        map.remove("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            String merchant_no = map.get("merchant_no").toString();
            String out_order_no = map.get("out_order_no").toString();
            String amount = map.get("amount").toString();
            String pay_type = map.get("pay_type").toString();
            String notify_url = map.get("notify_url").toString();
            //（ 商户密钥）
            final String signContentStr = merchant_no + out_order_no + amount + pay_type + notify_url + secret;
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
        String respStr = "{\n" +
                "    \"code\": 1,\n" +
                "    \"data\": {\n" +
                "        \"merchant_no\": \"100001045907028\",\n" +
                "        \"amount\": \"100\",\n" +
                "        \"sign\": \"483e376a11676a7b4d072ae08dc3b664\",\n" +
                "        \"out_order_no\": \"P1734850732227571714\",\n" +
                "        \"pay_type\": \"alipay\",\n" +
                "        \"notify_url\": \"http://pay-api.eluosi-pay.com/api/pay/notify/zhouyi/P1734850732227571714\",\n" +
                "        \"card_type\": \"woem\"\n" +
                "    },\n" +
                "    \"success\": true\n" +
                "}";
        JSONObject jsonObject = JSONObject.parseObject(respStr);
        Map map = JSON.parseObject(jsonObject.getJSONObject("data").toJSONString());
        String secret = "8a6c783a1e877dc4ebc53c68d201c370";


        String merchant_no = map.get("merchant_no").toString();
        String out_order_no = map.get("out_order_no").toString();
        String amount = map.get("amount").toString();
        String pay_type = map.get("pay_type").toString();
        String notify_url = map.get("notify_url").toString();
        //（ 商户密钥）
        final String signContentStr = merchant_no + out_order_no + amount + pay_type + notify_url + secret;
        log.info(signContentStr);
        final String signStr = SignatureUtils.md5(signContentStr).toUpperCase();
        log.info(signStr);

    }
}