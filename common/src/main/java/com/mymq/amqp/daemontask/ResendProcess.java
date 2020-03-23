package com.mymq.amqp.daemontask;


import com.mymq.amqp.config.RabbitTemplateConfig;
import com.mymq.amqp.sender.RabbitMQSender;
import com.mymq.amqp.core.AlertSender;
import com.mymq.amqp.core.DBCoordinator;
import com.mymq.amqp.config.MQConstants;
import com.mymq.amqp.core.RabbitMetaMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ResendProcess implements SchedulingConfigurer {
    private Logger logger = LoggerFactory.getLogger(RabbitTemplateConfig.class);

    @Autowired
    DBCoordinator dbCoordinator;

    @Autowired
    RabbitMQSender rabbitSender;

    @Autowired
    AlertSender alertSender;

    /**prepare状态的消息超时告警*/
    public void alertPrepareMsg() throws Exception{
        List<String> messageIds = dbCoordinator.getMsgPrepare();
        for(String messageId: messageIds){
            alertSender.doSend(messageId);
        }
    }


    public void resendReadyMsg() throws Exception{
        List<RabbitMetaMessage> messages = dbCoordinator.getMsgReady();
        for(RabbitMetaMessage message: messages){
            long msgCount = dbCoordinator.getResendValue(MQConstants.MQ_RESEND_COUNTER,message.getMessageId());
            if( msgCount > MQConstants.MAX_RETRY_COUNT){
                alertSender.doSend(message.getMessageId());
            }
            rabbitSender.send(message);
            dbCoordinator.incrResendKey(MQConstants.MQ_RESEND_COUNTER, message.getMessageId());
        }
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar)
    {
        taskRegistrar.setTaskScheduler(new ThreadPoolTaskScheduler());
        taskRegistrar.getScheduler().schedule(
                new Runnable() { @Override public void run() {
                    try {
                        log.info("开始执行定时任务");
                        resendReadyMsg();
                        log.info("结束执行定时任务");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                }, new CronTrigger("*/30 * * * * ?"));
    }



}
