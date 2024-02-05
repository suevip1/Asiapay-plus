package com.jeequan.jeepay.pay.channel.languifang;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
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
public class LanguifangChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[兰桂坊支付]";

    private static final String ON_FAIL = "failed";

    private static final String ON_SUCCESS = "success";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.LANGUIFANG;
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

            //查单
            NormalMchParams normalMchParams = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), NormalMchParams.class);
            Map<String, Object> map = new HashMap<>();
            String key = normalMchParams.getSecret();

            String merchant_no = normalMchParams.getMchNo();
            String order_no = payOrder.getPayOrderId();

            map.put("merchant_no", merchant_no);
            map.put("order_no", order_no);

            String signContent = "key=" + merchant_no + key + "&merchant_no=" + merchant_no + "&order_no=" + order_no;
            String sign = JeepayKit.md5(signContent).toLowerCase();

            map.put("sign", sign);

            String payGateway = normalMchParams.getQueryUrl();

            // 发送POST请求并指定JSON数据
            HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                    .execute();
            // 处理响应
            String raw = response.body();
            log.info("{} 查单返回：{}", LOG_TAG, raw);

            JSONObject queryResp = JSONObject.parseObject(raw);
            //只有请求code为200，success为true时，order_status = 6时表示订单支付成功
            if (queryResp.getString("code").equals("200")) {
                int state = queryResp.getJSONObject("data").getInteger("order_status");
                if (state == 6) {
                    //验签成功后判断上游订单状态
                    result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    return result;
                }
            }
            log.info("{}回调通知订单状态错误:{}", LOG_TAG, raw);
            result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
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

        if (resultsParam != null) {
            String secret = resultsParam.getSecret();
            String signContent = "key=" + resultsParam.getMchNo() + secret + "&order_amount=" + txnAmt + "&order_no=" + orderNo + "&pay_code=" + map.get("pay_code");
            String signStr = com.jeequan.jeepay.core.utils.JeepayKit.md5(signContent).toLowerCase();

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
        Map<String, Object> map = new HashMap<>();
        String key = "155512d37e92584731a7e4dfaa9e2e74";

        String merchant_no = "62642580";
        String order_no = "P1690580665751347201";

        map.put("merchant_no", merchant_no);
        map.put("order_no", order_no);

        String signContent = "key=" + merchant_no + key + "&merchant_no=" + merchant_no + "&order_no=" + order_no;
        String sign = JeepayKit.md5(signContent).toLowerCase();

        map.put("sign", sign);

        String payGateway = "http://api.languifangpay.top/v1/api/search_order";

        // 发送POST请求并指定JSON数据
        HttpResponse response = HttpUtil.createPost(payGateway).body(JSONObject.toJSON(map).toString()).contentType("application/json").timeout(10000) // 指定请求体的Content-Type为JSON
                .execute();
        // 处理响应
        String raw = response.body();
        log.info("{} 查单返回：{}", LOG_TAG, raw);
    }
}