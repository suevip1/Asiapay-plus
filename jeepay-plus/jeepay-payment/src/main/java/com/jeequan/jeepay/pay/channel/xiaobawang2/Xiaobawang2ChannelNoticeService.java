package com.jeequan.jeepay.pay.channel.xiaobawang2;

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
public class Xiaobawang2ChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[小霸王2支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "0000";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.XIAOBAWANG2;
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

            //msg SUCCESS
            String status = jsonParams.getString("msg");

            if (!status.equals("SUCCESS")) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, status);
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            } else {
                String channelOrderId = jsonParams.getString("serial");
                //验签成功后判断上游订单状态
                result.setChannelOrderId(channelOrderId);
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
        String orderNo = jsonParams.getString("outtradeno");        // 商户订单号
        String txnAmt = jsonParams.getString("fee");        // 支付金额

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
            final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, null) + "&keyvalue=" + secret;
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
        String str = "{\n" +
                "    \"msg\": \"SUCCESS\",\n" +
                "    \"uid\": \"549\",\n" +
                "    \"code\": \"0000\",\n" +
                "    \"serial\": \"XBW2308160041073410549547\",\n" +
                "    \"attachCode\": \"\",\n" +
                "    \"fee\": \"200000\",\n" +
                "    \"sign\": \"C2883E462CECBC889CD20103FB262219\",\n" +
                "    \"payment\": \"XBW2308160041073410549547\",\n" +
                "    \"attach\": \"\",\n" +
                "    \"outtradeno\": \"P1691490210497548289\"\n" +
                "}";


        String key = "BD35F7BD037F7FCC675313324F414009";
        Map map = JSON.parseObject(JSONObject.parseObject(str).toJSONString());
//        String sign = JeepayKit.getSign(map, key).toUpperCase();
        final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign"}) + "&keyvalue=" + key;
        log.info("[{}]请求响应:{}", LOG_TAG, signContentStr);
        final String signStr = SignatureUtils.md5(signContentStr).toUpperCase();

        log.info("[{}]请求响应:{}", LOG_TAG, signStr);

    }
}