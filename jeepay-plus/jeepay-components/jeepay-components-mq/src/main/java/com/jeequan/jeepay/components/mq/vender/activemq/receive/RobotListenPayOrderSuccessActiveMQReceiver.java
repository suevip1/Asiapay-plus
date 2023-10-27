package com.jeequan.jeepay.components.mq.vender.activemq.receive;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.executor.MqThreadExecutor;
import com.jeequan.jeepay.components.mq.model.RobotListenPayOrderSuccessMQ;
import com.jeequan.jeepay.components.mq.vender.IMQMsgReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ACTIVE_MQ)
@ConditionalOnBean(RobotListenPayOrderSuccessMQ.IMQReceiver.class)
public class RobotListenPayOrderSuccessActiveMQReceiver implements IMQMsgReceiver {

    @Autowired
    private RobotListenPayOrderSuccessMQ.IMQReceiver mqReceiver;

    /** 接收 【 queue 】 类型的消息 **/
    @Override
    @Async(MqThreadExecutor.EXECUTOR_PAYORDER_MCH_NOTIFY)
    @JmsListener(destination = RobotListenPayOrderSuccessMQ.MQ_NAME)
    public void receiveMsg(String msg){
        mqReceiver.receive(RobotListenPayOrderSuccessMQ.parse(msg));
    }

}