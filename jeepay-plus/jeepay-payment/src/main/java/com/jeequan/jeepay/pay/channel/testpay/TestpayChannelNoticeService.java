package com.jeequan.jeepay.pay.channel.testpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class TestpayChannelNoticeService extends AbstractChannelNoticeService {
    @Override
    public String getIfCode() {
        return CS.IF_CODE.TESTPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {
        try {
            JSONObject paramsReal = getReqParamJSON();
            log.info("TestPay 回调参数, jsonParams：{}", paramsReal.toJSONString());
            JSONObject params = new JSONObject();
            params.put("state","1");
            return MutablePair.of(urlOrderId, params);
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, PayPassage payPassage, NoticeTypeEnum noticeTypeEnum) {

        //notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null

        ChannelRetMsg result = ChannelRetMsg.confirmSuccess(null);
        //订单业务状态
        result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        //给回调方的返回
        result.setResponseEntity(textResp("ok"));


//        try {
//            ChannelRetMsg result = ChannelRetMsg.confirmSuccess(null);
//            // 获取请求参数
//            JSONObject jsonParams = (JSONObject) params;
//            log.info("{} 回调参数, jsonParams：{}", logPrefix, jsonParams);
//
//            // 校验支付回调
//            boolean verifyResult = verifyParams(jsonParams, payOrder, mchAppConfigContext);
//            // 验证参数失败
//            if (!verifyResult) {
//                //回调参数有问题得
//                throw ResponseException.buildText("fail");
//            }
//            log.info("{}验证支付通知数据及签名通过", logPrefix);
//
//
//            //todo 检查订单状态
//            if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
//                throw ResponseException.buildText("ok");
//            }
//
//            ResponseEntity okResponse = textResp("ok");
//            result.setResponseEntity(okResponse);
//
//            //存回调
//            payOrder.setChannelExtra(jsonParams.toJSONString());
//            payOrderService.updateById(payOrder);
//
//            //todo 验证金额
//            // 核对金额
//            String txnAmt = jsonParams.getString("RealValue");        // 支付金额
//            long dbPayAmt = payOrder.getAmount().longValue();
//            //校验转换后的金额
//            if (payOrder.getExtParam() != null) {
//                if (!payOrder.getExtParam().equals("")) {
//                    CastAmountData castAmountData = JSON.parseObject(payOrder.getExtParam(), CastAmountData.class);
//                    dbPayAmt = castAmountData.getPayAmount();
//                }
//            }
//
//            if (dbPayAmt != Long.parseLong(txnAmt) * 100) {
//                log.info("订单金额与参数金额不符。 dbPayAmt={}, txnAmt={}, payOrderId={}", dbPayAmt, txnAmt, payOrder.getPayOrderId());
//                throw ResponseException.buildText("ok");
//            }
//
//            int Code = jsonParams.getInteger("Code");  // 订单状态
//            //订单状态检查 Code	状态码	是	Int	0正在处理中，2成功，3失败
//            if (Code != 2) {
//                log.info("回调通知订单状态错误 [Code] :{}", Code);
//                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
//                payOrderCardInfoService.UpdateCardInfoStatusError(payOrder);
//            } else {
//                //验签成功后判断上游订单状态
//                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
//                //更新cardInfoOrder
//                payOrderCardInfoService.UpdateCardInfoStatusSuccess(payOrder);
//            }
//            return result;
//
//        } catch (Exception e) {
////            log.error("error", e);
//            throw ResponseException.buildText("ERROR");
//        }
        return result;
    }
}
