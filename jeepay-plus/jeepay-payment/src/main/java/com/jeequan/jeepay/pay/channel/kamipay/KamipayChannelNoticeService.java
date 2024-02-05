package com.jeequan.jeepay.pay.channel.kamipay;

import cn.hutool.http.HttpUtil;
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
import com.jeequan.jeepay.pay.util.BigDecimalUtil;
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
public class KamipayChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[大富卡密支付2]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.KAMIPAY;
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

            //支付状态 成功 1
            int status = jsonParams.getInteger("status");

            if (status != 1) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, status);
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            } else {
                String channelOrderId = jsonParams.getString("pt_order_id");
                result.setChannelOrderId(channelOrderId);
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
        String orderNo = jsonParams.getString("mch_order_id");        // 商户订单号
        String txnAmt = jsonParams.getString("price");        // 支付金额

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

        NormalMchParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

        String status = jsonParams.getString("status");
        String pt_order_id = jsonParams.getString("pt_order_id");
        String mchid = resultsParam.getMchNo();

        String sign = jsonParams.getString("sign");
        Map<String, Object> map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (resultsParam != null) {
            //商户订单号+订单描述+通道类型+金额+实际支付金额+商户号 + 商户密钥
            String secret = resultsParam.getSecret();
            final String signContentStr = mchid + orderNo + pt_order_id + txnAmt + status + secret;
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
                "    \"mchid\": \"217\",\n" +
                "    \"price\": \"99.00\",\n" +
                "    \"mch_order_id\": \"P1751568477223133186\",\n" +
                "    \"sign\": \"6c1bbe15bdbb8c08870bcb74cc946053\",\n" +
                "    \"pt_order_id\": \"p202401281930425244\",\n" +
                "    \"paytype\": \"1\",\n" +
                "    \"status\": \"1\"\n" +
                "}";

        JSONObject jsonParams = JSON.parseObject(test);

        BigDecimal channelNotifyAmount = new BigDecimal("99.00");
        BigDecimal orderAmount = BigDecimalUtil.INSTANCE.divide(9900L, 100f);
        log.info(channelNotifyAmount.toString());
        log.info(orderAmount.toString());

        String mch_order_id = jsonParams.getString("mch_order_id");        // 商户订单号
        String price = jsonParams.getString("price");        // 支付金额

        String secret = "27fc325ee61574267d20351dc38a326e";

        String status = jsonParams.getString("status");
        String pt_order_id = jsonParams.getString("pt_order_id");
        String mchid = "9000217";

        //sign=md5(mchid+mch_order_id+pt_order_id+price+status+商户秘钥)；
        final String signContentStr = mchid + mch_order_id + pt_order_id + price + status + secret;
        final String signStr = SignatureUtils.md5(signContentStr).toLowerCase();

        log.info(signStr);
    }
}