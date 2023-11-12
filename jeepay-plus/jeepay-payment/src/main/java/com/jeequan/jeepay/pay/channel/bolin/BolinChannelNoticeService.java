package com.jeequan.jeepay.pay.channel.bolin;

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
public class BolinChannelNoticeService extends AbstractChannelNoticeService {

    private static final String LOG_TAG = "[铂霖支付]";

    private static final String ON_FAIL = "fail";

    private static final String ON_SUCCESS = "OK";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.BOLIN;
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

            //查单
            Map<String, Object> map = new HashMap<>();
            String pay_orderid = jsonParams.getString("orderid");
            BolinParamsModel bolinParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), BolinParamsModel.class);
            String key = bolinParamsModel.getSecret();
            String requestPrivateKey= bolinParamsModel.getRequestPrivateKey();
            map.put("pay_memberid", bolinParamsModel.getMchNo());
            map.put("pay_orderid", pay_orderid);
            String signValue = JeepayKit.getSign(map, key).toUpperCase();
            String pay_md5sign = SignatureUtils.buildSHA256WithRSASignByPrivateKey(signValue, requestPrivateKey);
            map.put("pay_md5sign", pay_md5sign);

            String raw = HttpUtil.post(bolinParamsModel.getQueryUrl(), map, 10000);
            log.info("{} 查单请求响应:{}", LOG_TAG, raw);
            JSONObject queryResult = JSON.parseObject(raw, JSONObject.class);

            //只有请求code为200，success为true时，order_status = 6时表示订单支付成功
            if (queryResult.getString("returncode").equals("00")) {
                String trade_state = queryResult.getString("trade_state");
                if (trade_state.equals("SUCCESS")) {
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
        String orderNo = jsonParams.getString("orderid");        // 商户订单号
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
        BigDecimal orderAmount = new BigDecimal(payOrder.getAmount() / 100f);

        BolinParamsModel bolinParamsModel = JSONObject.parseObject(payPassage.getPayInterfaceConfig(), BolinParamsModel.class);

        String sign = jsonParams.getString("sign");
        Map map = JSON.parseObject(jsonParams.toJSONString());
        map.remove("sign");
        if (bolinParamsModel != null) {
            String secret = bolinParamsModel.getSecret();
            String requestPublicKey = bolinParamsModel.getRequestPublicKey();
            final String signValue = JeepayKit.getSign(map, secret).toUpperCase();
            boolean verifySign = SignatureUtils.buildSHA256WithRSAVerifyByPublicKey(signValue, requestPublicKey, sign);
            if (verifySign && orderAmount.compareTo(channelNotifyAmount) == 0) {
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

    public static void main(String[] args){
        String jsonParams = "{\"transaction_id\":\"202311011150221011018e0\",\"amount\":\"100.0000\",\"datetime\":\"20231101115146\",\"orderid\":\"P1719562500768845825\",\"returncode\":\"00\",\"sign\":\"N5xLO9S58WW1hrGEi1zETz/3KPhQi41dexl/+tml9qdJ0+DqgApINOaMT71vDB/CQzxRylBP70E06d5JupOxQgtAl/GR7RCt95LDH7gan0g3YGD00hoJzWhwdFWpuDiaU85eHlG/yPgl08mGsn1kC1d6OH6vnIFGm24Q9o9ggS9I9SmMP9H/A24yhcx8KKOZMU9AzozH+kJZYiBBGGqx9WhkTntCNDEL8aKsiq4LCFWLcN0YMoX1uBe/KxOq0VlkPPQrzkvFuDJHM0sZN/KTbsgi8LoBcJ8i3HBjL43TQZ66RW6eQhGZjkML30+OTm7sW+aCLRSwTNDOOdkKuzktdg==\",\"memberid\":\"231106447\"}";
        Map map = JSON.parseObject(jsonParams);
        String sign = map.get("sign").toString();
        System.out.println("返回Sign："+ sign);
        String secret = "DLbxBrQBIplN2gGTba5CgkT1O4mPR6yJ";
        String requestPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnVcZhlDHBYXgB2exj+9VpoxzqG/NnxGxwToyUmYmbWshyOc0eCLQA+5WfN0pzZyQGfjEpHD7SPvDuRqqJPeNd/vVKMSIeM6mU5vpyQdrQvUq8buuSskGOO0qcd/Uiww6ToTkHJdfXTFO/FpI/MiuvYsIDW132dnageXu/l2iI9PbA2fkrnDxl6/loC2UiPnHJfyWbD3h/Bwj0uNkBrVp56BEQFDrxhbn7rAC/4Kg6lpexEJ1VbA1z/5MFhvDWTQ/Pw2QW4NfDP834kT4AuH2kfyYLG5gHBpcbGuyyJjt7rgwWSDMip0S6J2kgkKDdVmG+B3VNQs3yxeRzOWXoiNNowIDAQAB";
        map.remove("sign");

        String signValue = JeepayKit.getSign(map, secret).toUpperCase();
        System.out.println("MD5后字符串："+ signValue);
        System.out.println("公钥："+ requestPublicKey);
        boolean verifySign = SignatureUtils.buildSHA256WithRSAVerifyByPublicKey(signValue, requestPublicKey, sign);
        System.out.println(verifySign);
    }
}