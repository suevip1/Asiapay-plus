package com.jeequan.jeepay.pay.channel.sgpay;

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
public class SgpayChannelNoticeService extends AbstractChannelNoticeService {
    private static final String LOG_TAG = "[SG支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.SGPAY;
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

            ///查单
            NormalMchParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);
            String key = normalMchParams.getSecret();

            Map<String, Object> map = new HashMap<>();
            String merchantId = normalMchParams.getMchNo();
            String mcOrderNum = payOrder.getPayOrderId();
            String timestamp = System.currentTimeMillis() / 1000 + "";
            map.put("merchantId", merchantId);
            map.put("mcOrderNum", mcOrderNum);
            map.put("timestamp", timestamp);
            String sign = JeepayKit.getSign(map, key);
            map.put("sign", sign);

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(normalMchParams.getQueryUrl()).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            String raw = response.body();
            log.info("{} 查单请求响应:{}", LOG_TAG, raw);
            JSONObject queryResult = JSON.parseObject(raw, JSONObject.class);

            //	0：支付成功，其他值为失败
            int status = queryResult.getInteger("code");
            JSONObject resultData = queryResult.getJSONObject("data");
            //status	int	0未支付 1支付成功
            if (!(status == 0 && resultData != null && resultData.getString("status").equals("1"))) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, resultData.getString("status"));
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
        String orderNo = jsonParams.getString("mcOrderNum");        // 商户订单号
        String txnAmt = jsonParams.getString("orderAmount");        // 支付金额

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
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"sign"}) + "&key=" + secret;
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
        String respStr = "{\"orderAmount\":100,\"actualReceiptAmount\":100,\"merchantId\":864,\"mcOrderNum\":\"P1734620252056084482\",\"sign\":\"5294F26B1BE689281F2BA771F860C855\",\"orderNum\":\"2312133870578804887636153\",\"timestamp\":1702400942}";
        JSONObject jsonObject = JSONObject.parseObject(respStr);
        String secret = "abbb3d704ea2862608d5b469e0069864";
        final String signContentStr = SignatureUtils.getSignContentFilterEmpty(jsonObject, new String[]{"sign"}) + "&key=" + secret;
        log.info(signContentStr);
        final String signStr = SignatureUtils.md5(signContentStr).toUpperCase();
        log.info(signStr);

        Map<String, Object> map = new HashMap<>();
        String merchantId = "864";
        String mcOrderNum = "P1734620252056084482";
        String timestamp = System.currentTimeMillis() / 1000 + "";
        map.put("merchantId", merchantId);
        map.put("mcOrderNum", mcOrderNum);
        map.put("timestamp", timestamp);
        String sign = JeepayKit.getSign(map, secret);
        map.put("sign", sign);

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost("http://shopapi.sgxyzpay.com/api/order/query").body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
        // 处理响应
        String raw = response.body();
        log.info("{} 查单请求响应:{}", LOG_TAG, raw);

        JSONObject queryResult = JSON.parseObject(raw, JSONObject.class);
        int status = queryResult.getInteger("code");
        JSONObject resultData = queryResult.getJSONObject("data");
        //status	int	0未支付 1支付成功
        if (!(status == 0 && resultData != null && resultData.getString("status").equals("1"))) {
            log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, status);
        } else {
            //验签成功后判断上游订单状态
            log.info("[{}]回调通知订单状态success:{}", LOG_TAG, status);
        }
    }
}