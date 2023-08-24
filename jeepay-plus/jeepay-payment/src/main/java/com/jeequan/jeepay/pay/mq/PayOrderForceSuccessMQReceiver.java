package com.jeequan.jeepay.pay.mq;

import com.jeequan.jeepay.components.mq.model.PayOrderForceSuccessMQ;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单统计异步MQ接收
 */
@Slf4j
@Component
public class PayOrderForceSuccessMQReceiver implements PayOrderForceSuccessMQ.IMQReceiver {

    @Autowired
    private PayOrderProcessService payOrderProcessService;

    @Override
    public void receive(PayOrderForceSuccessMQ.MsgPayload payload) {
        try {
            PayOrder payOrder = payload.getPayOrder();
            log.info("接收强制补单通知,订单号{}", payOrder.getPayOrderId());
            payOrderProcessService.confirmSuccess(payOrder);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}