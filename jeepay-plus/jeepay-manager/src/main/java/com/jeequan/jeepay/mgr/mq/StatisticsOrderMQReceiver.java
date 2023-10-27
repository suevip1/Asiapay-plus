package com.jeequan.jeepay.mgr.mq;

import cn.hutool.json.JSONUtil;
import com.jeequan.jeepay.components.mq.model.RobotListenPayOrderSuccessMQ;
import com.jeequan.jeepay.components.mq.model.StatisticsOrderMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.service.CommonService.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 订单统计异步MQ接收
 */
@Slf4j
@Component
public class StatisticsOrderMQReceiver implements StatisticsOrderMQ.IMQReceiver {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private IMQSender mqSender;


    @Override
    public void receive(StatisticsOrderMQ.MsgPayload payload) {
        try {
            PayOrder payOrder = payload.getPayOrder();
            //基数统计支付中以及出码失败的订单
            if (payOrder.getState() == PayOrder.STATE_ING || payOrder.getState() == PayOrder.STATE_ERROR) {
                statisticsService.PushPayOrderToCache(payOrder);
                RedisUtil.storeObjectWithExpiration(payOrder.getPayOrderId(), payOrder, CS.REAL_TIME_STAT);
            } else if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
                statisticsService.PushSuccessPayOrderToCache(payOrder);
                RedisUtil.storeObjectWithExpiration(payOrder.getPayOrderId(), payOrder, CS.REAL_TIME_SUCCESS_STAT);
                //todo 发mq到机器人 只发成功的订单
                mqSender.send(RobotListenPayOrderSuccessMQ.build(payOrder.getPayOrderId(), payOrder.getMchOrderNo(), payOrder.getPassageOrderNo()));

            } else if (payOrder.getState() == PayOrder.STATE_REFUND) {
                //测试冲正订单
                payOrder.setAmount(payOrder.getAmount() * -1);
                payOrder.setMchFeeAmount(payOrder.getMchFeeAmount() * -1);
                payOrder.setPassageFeeAmount(payOrder.getPassageFeeAmount() * -1);
                payOrder.setAgentFeeAmount(payOrder.getAgentFeeAmount() * -1);
                payOrder.setAgentPassageFee(payOrder.getAgentPassageFee() * -1);
                statisticsService.PushSuccessPayOrderToCache(payOrder);
                log.info("StatisticsOrderMQReceiver-[{}]-测试冲正订单 {}", payOrder.getPayOrderId(), JSONUtil.toJsonStr(payOrder));
            } else {
                log.error("StatisticsOrderMQReceiver-[{}]-待统计订单状态错误 {}", payOrder.getPayOrderId(), JSONUtil.toJsonStr(payOrder));
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}