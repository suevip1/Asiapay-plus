package com.jeequan.jeepay.pay.channel.x5pay;


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
import java.util.Map;

@Slf4j
@Service
public class X5payChannelNoticeService extends AbstractChannelNoticeService {
    private static final String LOG_TAG = "[X5支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "SUCCESS";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.X5PAY;
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

            //支付状态：1为支付成功 0为未支付 -1已过期
            String tradeStatus = jsonParams.getString("tradeStatus");

            if (!tradeStatus.equals("1")) {
                log.info("[{}]回调通知订单状态错误:{}", LOG_TAG, tradeStatus);
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
        String orderNo = jsonParams.getString("billNo");        // 商户订单号
        String txnAmt = jsonParams.getString("totalAmount");        // 支付金额

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
            final String signContentStr = SignatureUtils.getSignContentFilterEmpty(map, new String[]{"attach"}) + "&key=" + secret;
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
        String str = "{\"mchId\":\"600600297\",\"code\":\"Hystk\",\"tradeNo\":\"62484706988947775964\",\"sign\":\"13AE8BB5509E11F719C2341F2AD8E1A7\",\"transferId\":\"62484706988947775964\",\"acc_key\":\"A302800600842707\",\"way\":\"wap\",\"totalAmount\":\"100.00\",\"acc_name\":\"商通\",\"tradeStatus\":\"0\",\"payment\":\"alipay\",\"attach\":\"\",\"create_at\":\"1709383484\",\"billNo\":\"P1763908391453409282\"}";

        JSONObject jsonParams = JSONObject.parseObject(str);
        String sign = jsonParams.getString("sign");
        log.info("回调签名："+sign);
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        String secret = "d2547df0ce57bf9e7aae42c4ff11f33b3bc93372479594d8cbb1e7dc7c162e3b";
        String signContentStr = SignatureUtils.getSignContent(map, null, new String[]{""}) + "&key=" + secret;
        log.info("待签名串："+signContentStr);
        String signStr = SignatureUtils.md5(signContentStr).toUpperCase();
        log.info("验证签名："+signStr);
    }
}
