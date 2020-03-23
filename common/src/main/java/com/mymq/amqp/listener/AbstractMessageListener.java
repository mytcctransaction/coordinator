
package com.mymq.amqp.listener;

import com.mymq.amqp.core.DBCoordinator;
import com.mymq.amqp.config.MQConstants;
import com.mymq.amqp.core.RabbitMetaMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;

/**
 * <p><b>Description:</b> RabbitMQ抽象消息监听，所有消息消费者必须继承此类
 * <p><b>Company:</b> 
 *
 *
 * @version V0.1
 */
public abstract class AbstractMessageListener implements ChannelAwareMessageListener {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    ApplicationContext applicationContext;

    /**
     * 接收消息，子类必须实现该方法
     *
     * @param message          消息对象
     */
    public abstract void receiveMessage(Message message);

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        Long deliveryTag = messageProperties.getDeliveryTag();
        String msgId=messageProperties.getMessageId();
        ObjectMapper mapper = new ObjectMapper();
        RabbitMetaMessage rabbitMetaMessageTemp = null;
        try {
            rabbitMetaMessageTemp = mapper.readValue(message.getBody(), RabbitMetaMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        DBCoordinator coordinator = (DBCoordinator)applicationContext.getBean(rabbitMetaMessageTemp.getDbCoordinator());
        Long consumerCount=coordinator.incrementHashItem(MQConstants.MQ_CONSUMER_RETRY_COUNT_KEY, msgId, 1);
        //Long consumerCount = redisTemplate.opsForHash().increment(MQConstants.MQ_CONSUMER_RETRY_COUNT_KEY, msgId, 1);

        logger.info("收到消息,当前消息ID:{} 消费次数：{}", messageProperties.getMessageId(), consumerCount);

        try {
            receiveMessage(message);
            //int a=1/0;
            // 成功的回执
            channel.basicAck(deliveryTag, false);

            setConsumeSuccess(msgId,message,coordinator);
        } catch (Exception e) {
            logger.error("RabbitMQ 消息消费失败，" + e.getMessage(), e);
            if (consumerCount >= MQConstants.MAX_CONSUMER_COUNT) {
                // 入死信队列 true 退回队列，false 扔掉消息
                channel.basicReject(deliveryTag, false);
            } else {
                // 重回到队列，重新消费, 按照2的指数级递增
                Thread.sleep((long) (Math.pow(MQConstants.BASE_NUM, consumerCount)*1000));
                redisTemplate.opsForHash().increment(MQConstants.MQ_CONSUMER_RETRY_COUNT_KEY,
                        messageProperties.getMessageId(), 1);
                channel.basicNack(deliveryTag, false, true);
            }

        }


    }

    private void setConsumeSuccess(String msgId,Message message,DBCoordinator coordinator){
        ObjectMapper mapper = new ObjectMapper();
        RabbitMetaMessage rabbitMetaMessageTemp = null;
        try {
            rabbitMetaMessageTemp = mapper.readValue(message.getBody(), RabbitMetaMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // 如果消费成功，将Redis中统计消息消费次数的缓存删除
        //redisTemplate.opsForHash().delete(MQConstants.MQ_CONSUMER_RETRY_COUNT_KEY, msgId);
        coordinator.delHashItem(MQConstants.MQ_CONSUMER_RETRY_COUNT_KEY, msgId);
        coordinator.setMessageStatus(msgId,MQConstants.CONSUME_SUCCESS,rabbitMetaMessageTemp);
    }

}
