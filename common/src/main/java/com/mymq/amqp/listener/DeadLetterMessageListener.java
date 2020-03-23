package com.mymq.amqp.listener;

import com.mymq.amqp.config.MQConstants;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterMessageListener implements ChannelAwareMessageListener {
    private Logger logger = LoggerFactory.getLogger(DeadLetterMessageListener.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

		/*@Autowired
		private DeadLetterMessageMapper deadLetterMessageMapper;

		@Autowired
		private MailServiceImpl mailService;*/

    // 收件人
		/*@Value("${recipient.email.address}")
		private String emailRecipient;*/


    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        // 消息体
        String messageBody = new String(message.getBody());

        logger.warn("dead letter message：{} | tag：{}", messageBody, message.getMessageProperties().getDeliveryTag());
        /*// 入库
        insertRecord(logKey, message);
        // 发邮件
        sendEmail(logKey, messageProperties.getMessageId(), messageBody);*/

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        //channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
        redisTemplate.opsForHash().delete(MQConstants.MQ_CONSUMER_RETRY_COUNT_KEY, messageProperties.getMessageId());
    }

}