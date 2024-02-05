package com.jeequan.jeepay.pay.channel.wlan;

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
public class WlanChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[Wlan支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WLAN;
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

            //status string 状态码：success 成
            String status = jsonParams.getString("status");

            if (!status.equalsIgnoreCase("success")) {
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
        String txnAmt = jsonParams.getString("amount");        // 支付金额

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
        map.remove("sign");

        String rk = jsonParams.getString("rk");


        if (resultsParam != null) {
            String secret = resultsParam.getSecret();

            String signStr = SignatureUtils.getSignContentFilterEmpty(map, null);
            String signStr1 = signStr + "&key" + rk + "=" + secret;
            String signResult = SignatureUtils.md5(SignatureUtils.md5(signStr1)).toUpperCase();

            if (signResult.equalsIgnoreCase(sign) && orderAmount.compareTo(channelNotifyAmount) == 0) {
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
        String testStr = "{\"uid\":\"2333\",\"amount\":\"100.00\",\"signature\":\"e51dc61cbecf8c5423cf7da7f0fe78ff\",\"orderStatus\":\"ok\",\"remark\":\"___强制回调不结算——\",\"transNo\":\"P1746849387908763650\"}";

        JSONObject jsonParams = JSONObject.parseObject(testStr);

        String sign = jsonParams.getString("signature");
        Map<String, Object> map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("signature");
        map.remove("remark");

        String secret = "da7b1429af1209b4ca1243e25d96c7bc4a4888d9d7ff1139bb9c669721eb8d80e4e1f99c6bcde7d7dd8383d22defd8ccc87a34cd79e0c4ae4d2169e1607e0fd170645cc8bf6c25063c120d1e6244885cf20b5be137d19ff85f4fee1fd36eeef3";

        final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + secret;
        log.info(signContentStr);
        final String signStr = SignatureUtils.md5(SignatureUtils.md5(signContentStr)).toLowerCase();

        log.info(signStr);
    }

}