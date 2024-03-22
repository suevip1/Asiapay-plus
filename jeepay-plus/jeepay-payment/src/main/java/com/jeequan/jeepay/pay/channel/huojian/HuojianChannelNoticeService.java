package com.jeequan.jeepay.pay.channel.huojian;


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
public class HuojianChannelNoticeService extends AbstractChannelNoticeService {
    private static final String LOG_TAG = "[火箭支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "ok";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.HUOJIAN;
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
            // 获取请求头参数
            String sign = request.getHeader("sign");
            log.info("{} 回调Header参数, sign：{}", LOG_TAG, sign);

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            log.info("{} 回调参数, jsonParams：{}", LOG_TAG, jsonParams);

            // 校验支付回调
            boolean verifyResult = verifyParams(sign, jsonParams, payOrder, payPassage);
            // 验证参数失败
            if (!verifyResult) {
                //回调参数有问题得
                throw ResponseException.buildText(ON_FAIL);
            }
            log.info("{}验证支付通知数据及签名通过", LOG_TAG);

            ResponseEntity okResponse = textResp(ON_SUCCESS);
            result.setResponseEntity(okResponse);

            //true:成功 false:失败
            Boolean paid = jsonParams.getBoolean("paid");

            if (paid) {
                //验签成功后判断上游订单状态
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            } else {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, paid);
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
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
    public boolean verifyParams(String sign, JSONObject jsonParams, PayOrder payOrder, PayPassage payPassage) {
        String orderNo = jsonParams.getString("order_no");        // 商户订单号
        String txnAmt = jsonParams.getString("amount_real");        // 支付金额

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

        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("extra");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            final String signContentStr = JSON.toJSONString(map);
            final String signStr = SignatureUtils.md5(signContentStr + secret).toUpperCase();
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
        //回调Header参数, sign：660FC642DADB7A9BD7D2B5323AD83077
        String jsonParams = "{\"order_no\":\"P1721392808686264321\",\"amount\":10000,\"time_expire\":1699247302766,\"subject\":\"subject\",\"channel\":\"code_apple\",\"body\":\"body\",\"time_settle\":1699247002766,\"time_paid\":1699247079000,\"amount_settle\":8700,\"amount_real\":10000,\"extra\":\"{}\",\"paid\":true,\"client_ip\":\"34.150.57.18\",\"currency\":\"cny\",\"refunded\":false,\"order_id\":\"202311061303221932948005294\",\"reversed\":false,\"transaction_no\":\"202311061303221932948005294\"}";
        Map map = JSON.parseObject(jsonParams);
//        map.remove("extra");
        String secret = "ebac10a900e9ebd54e8cdff0c971c35f";
        String signContentStr = JSON.toJSONString(map);
        String signStr = SignatureUtils.md5(signContentStr + secret).toUpperCase();
        System.out.println(signStr);
    }
}
