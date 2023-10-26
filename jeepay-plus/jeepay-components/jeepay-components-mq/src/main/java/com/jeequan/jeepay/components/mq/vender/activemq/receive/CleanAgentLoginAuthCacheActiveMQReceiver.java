package com.jeequan.jeepay.components.mq.vender.activemq.receive;


import com.jeequan.jeepay.components.mq.constant.MQVenderCS;
import com.jeequan.jeepay.components.mq.executor.MqThreadExecutor;
import com.jeequan.jeepay.components.mq.model.CleanAgentLoginAuthCacheMQ;
import com.jeequan.jeepay.components.mq.vender.IMQMsgReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ACTIVE_MQ)
@ConditionalOnBean(CleanAgentLoginAuthCacheMQ.IMQReceiver.class)
public class CleanAgentLoginAuthCacheActiveMQReceiver implements IMQMsgReceiver {

    @Autowired
    private CleanAgentLoginAuthCacheMQ.IMQReceiver mqReceiver;

    /** 接收 【 queue 】 类型的消息 **/
    @Override
    @Async(MqThreadExecutor.EXECUTOR_PAYORDER_MCH_NOTIFY)
    @JmsListener(destination = CleanAgentLoginAuthCacheMQ.MQ_NAME)
    public void receiveMsg(String msg){
        mqReceiver.receive(CleanAgentLoginAuthCacheMQ.parse(msg));
    }

}
