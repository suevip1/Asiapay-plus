package com.jeequan.jeepay.pay.channel.lansha;

import cn.hutool.http.HttpResponse;
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
public class LanshaChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[蓝鲨支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.LANSHA;
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
            map.put("merchantId", normalMchParams.getMchNo());
            String mcOrderNum = jsonParams.getString("mcOrderNum");
            map.put("mcOrderNum", mcOrderNum);
            Long timestamp = System.currentTimeMillis() / 1000;
            map.put("timestamp", timestamp);
            String signStr = SignatureUtils.getSignContent(map, null, null) + "&key=" + key;
            String sign = SignatureUtils.md5(signStr).toUpperCase();
            map.put("sign", sign);

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(normalMchParams.getQueryUrl()).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000).execute();
            // 处理响应
            String raw = response.body();
            log.info("{} 查单请求响应:{}", LOG_TAG, raw);
            JSONObject queryResult = JSON.parseObject(raw, JSONObject.class);
            //拉起订单成功
            if (queryResult.getString("code").equals("0")) {
                JSONObject data = queryResult.getJSONObject("data");
                //支付状态 0未支付 1支付成功
                int status = data.getInteger("status");

                if (status != 1) {
                    log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, status);
                    result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                } else {
                    //验签成功后判断上游订单状态
                    result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }
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

        BigDecimal channelNotifyAmount = new BigDecimal(txnAmt);
        BigDecimal orderAmount = BigDecimalUtil.INSTANCE.divide(payOrder.getAmount(), 100f);

        NormalMchParams resultsParam = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);

        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            String signContent = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + secret;
            String signStr = SignatureUtils.md5(signContent).toUpperCase();
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
        String str = "{\"orderAmount\":10,\"actualReceiptAmount\":10,\"merchantId\":758,\"mcOrderNum\":\"P1751236560635371522\",\"sign\":\"8650AB775BF03C2806321C327FB16E88\",\"orderNum\":\"2401277750728166457612957\",\"timestamp\":1706362487}";
        JSONObject jsonParams = JSONObject.parseObject(str);
        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        String secret = "82cf21c7cbfac20bc6275cb150fb015a";
        String signContent = SignatureUtils.getSignContentFilterEmpty(map, null) + "&key=" + secret;
        String signStr = SignatureUtils.md5(signContent).toLowerCase();
        log.info(signStr);
    }

}