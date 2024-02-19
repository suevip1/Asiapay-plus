package com.jeequan.jeepay.components.mq.vender.activemq.receive;

import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.executor.MqThreadExecutor;
import com.jeequan.jeepay.components.mq.model.RobotWarnMQ;
import com.jeequan.jeepay.components.mq.vender.IMQMsgReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ACTIVE_MQ)
@ConditionalOnBean(RobotWarnMQ.IMQReceiver.class)
public class RobotWarnMQActiveReceiver implements IMQMsgReceiver {

    @Autowired
    private RobotWarnMQ.IMQReceiver mqReceiver;

    /** 接收 【 queue 】 类型的消息 **/
    @Override
    @Async(MqThreadExecutor.EXECUTOR_PAYORDER_MCH_NOTIFY)
    @JmsListener(destination = RobotWarnMQ.MQ_NAME)
    public void receiveMsg(String msg){
        mqReceiver.receive(RobotWarnMQ.parse(msg));
    }

}