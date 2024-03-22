package com.jeequan.jeepay.pay.channel.liyupay2;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class Liyupay2ChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[鲤鱼支付2]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.LIYUPAY2;
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

            //绑定结果：200 绑定成功
            int bindState = jsonParams.getInteger("bindState");

            if (bindState != 200) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, bindState);
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
        BigDecimal orderAmount = new BigDecimal(payOrder.getAmount());

        NormalMchParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();

            List<String> fieldOrder = Arrays.asList("bindState", "merchantId", "merchantOrderNo", "orderNo", "amount");
            StringBuilder sb = new StringBuilder();
            for (String field : fieldOrder) {
                if (map.containsKey(field)) {
                    Object value = map.get(field);
                    sb.append(field).append("=").append(value).append("&");
                }
            }
            sb.append("secret=").append(secret);
            final String signContent = sb.toString();
            final String signStr = SignatureUtils.md5(signContent).toLowerCase();
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
        String jsonParams = "{\"amount\":10000,\"orderNo\":\"2402291723354738259614038\",\"bindState\":200,\"merchantId\":7399653,\"sign\":\"0289590e26e6060475843058d0c3b866\",\"merchantOrderNo\":\"P1763132897042731009\"}";
        Map map = JSON.parseObject(jsonParams);
        String sign = (String) map.get("sign");
        System.out.println(sign);
        map.remove("sign");

        String secret = "ab06692375740fbcdbb9123697923838";
        List<String> fieldOrder = Arrays.asList("bindState", "merchantId", "merchantOrderNo", "orderNo", "amount");
        StringBuilder sb = new StringBuilder();
        for (String field : fieldOrder) {
            if (map.containsKey(field)) {
                Object value = map.get(field);
                sb.append(field).append("=").append(value).append("&");
            }
        }
        sb.append("secret=").append(secret);

        String signContent = sb.toString();
        System.out.println(signContent);
        final String signStr = SignatureUtils.md5(signContent).toLowerCase();
        System.out.println(signStr);
    }
}