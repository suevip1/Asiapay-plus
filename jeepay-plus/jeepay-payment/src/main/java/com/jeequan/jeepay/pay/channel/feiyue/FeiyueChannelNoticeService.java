package com.jeequan.jeepay.pay.channel.feiyue;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
public class FeiyueChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[飞跃支付]";

    private static final String ON_FAIL = "failed";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.FEIYUE;
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
            log.info("验证支付通知数据及签名通过", LOG_TAG);

            ResponseEntity okResponse = textResp(ON_SUCCESS);
            result.setResponseEntity(okResponse);

            //状态： 6 为已支付完成
            String status = jsonParams.getString("status");

            if (!status.equals("6")) {
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
        String orderNo = jsonParams.getString("order_no");        // 商户订单号
        String txnAmt = jsonParams.getString("order_amount");        // 支付金额

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

        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        map.remove("platform_no");

        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign","platform_no"}) + "&key=" + secret;
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

        String test = "{\n" +
                "    \"order_no\": \"P1752226512607735809\",\n" +
                "    \"merchant_no\": \"76179693\",\n" +
                "    \"platform_no\": \"2024013015053033019312847\",\n" +
                "    \"create_time\": \"2024-01-30 15:05:30\",\n" +
                "    \"complete_time\": \"2024-01-30 15:06:51\",\n" +
                "    \"pay_code\": \"145582\",\n" +
                "    \"order_amount\": \"100.00\",\n" +
                "    \"sign\": \"a16f486d5b8aca13f26b59ba75c6cfb6\",\n" +
                "    \"attach\": \"attach\",\n" +
                "    \"status\": \"6\"\n" +
                "}";

        Map<String, Object> map = JSON.parseObject(JSONObject.toJSON(test).toString());

        String secret = "167ed78992d87776707519e4a539e31f";
        final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign","platform_no"}) + "&key=" + secret;

        log.info("{} 查单返回：{}", LOG_TAG, signContentStr);

        final String signStr = SignatureUtils.md5(signContentStr).toLowerCase();

        log.info("{} 查单返回：{}", LOG_TAG, signStr);
    }
}