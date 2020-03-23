package com.mymq.amqp.core;

import com.mymq.amqp.config.MQConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * redis协调者实现
 */
@Component(value = "redis")
public class DBRedisCoordinator implements DBCoordinator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void setMsgPrepare(String msgId) {
        redisTemplate.opsForSet().add(MQConstants.MQ_MSG_PREPARE, msgId);
    }

    @Override
    public void delMsgPrepare(String msgId) {
        redisTemplate.opsForSet().remove(MQConstants.MQ_MSG_PREPARE, msgId);
    }

    @Override
    public void setMsgReady(String msgId, RabbitMetaMessage rabbitMetaMessage) {
        redisTemplate.opsForHash().put(MQConstants.MQ_MSG_READY, msgId, rabbitMetaMessage);
        redisTemplate.opsForSet().remove(MQConstants.MQ_MSG_PREPARE,msgId);
    }

    @Override
    public void setMsgSuccess(String msgId) {
        RabbitMetaMessage rabbitMetaMessage=getMetaMsg(msgId);
        setMessageStatus(msgId,MQConstants.SEND_SUCCESS,rabbitMetaMessage);
        redisTemplate.opsForHash().delete(MQConstants.MQ_MSG_READY, msgId);

    }
    @Override
    public void delHashItem(String hashKey,String msgId){
        redisTemplate.opsForHash().delete(hashKey, msgId);
    }
    @Override
    public Long  incrementHashItem(String hashKey,String msgId,Object item){
        Long consumerCount = redisTemplate.opsForHash().increment(hashKey, msgId, 1);
        return consumerCount;
    }
    @Override
    public boolean putHashItem(String hashKey,String msgId,Object item){
        return redisTemplate.opsForHash().putIfAbsent(hashKey, msgId,item);
    }
    @Override
    public void setMessageStatus(String msgId, int status, RabbitMetaMessage rabbitMetaMessageTemp){

        if(MQConstants.SEND_SUCCESS==status){
            rabbitMetaMessageTemp.setSendStatus(status);
        }else if(MQConstants.CONSUME_SUCCESS==status){
            rabbitMetaMessageTemp.setConsumeStatus(status);
        }
        boolean b=redisTemplate.opsForHash().putIfAbsent(MQConstants.MQ_MSG_SUCCESS,msgId,rabbitMetaMessageTemp);
        if(!b){
            RabbitMetaMessage _rabbitMetaMessage=(RabbitMetaMessage)redisTemplate.opsForHash().get(MQConstants.MQ_MSG_SUCCESS,msgId);
            if(MQConstants.SEND_SUCCESS==status){
                _rabbitMetaMessage.setSendStatus(status);
            }else if(MQConstants.CONSUME_SUCCESS==status){
                _rabbitMetaMessage.setConsumeStatus(status);
            }
            redisTemplate.opsForHash().put(MQConstants.MQ_MSG_SUCCESS,msgId,_rabbitMetaMessage);
        }
    }

    @Override
    public RabbitMetaMessage getMetaMsg(String msgId) {
        return (RabbitMetaMessage)redisTemplate.opsForHash().get(MQConstants.MQ_MSG_READY, msgId);
    }

    @Override
    public List getMsgPrepare() throws Exception  {
        SetOperations setOperations = redisTemplate.opsForSet();
        Set<String> messageIds = setOperations.members(MQConstants.MQ_MSG_PREPARE);
        List<String> messageAlert = new ArrayList();
        for(String messageId: messageIds){
            /**如果消息超时，加入超时队列*/
            if(messageTimeOut(messageId)){
                messageAlert.add(messageId);
            }
        }
        /**在redis中删除已超时的消息*/
        setOperations.remove(MQConstants.MQ_MSG_PREPARE,messageAlert);
        return messageAlert;
    }

    @Override
    public List getMsgReady() throws Exception {
        HashOperations hashOperations = redisTemplate.opsForHash();
        List<RabbitMetaMessage> messages = hashOperations.values(MQConstants.MQ_MSG_READY);
        List<RabbitMetaMessage> messageAlert = new ArrayList();
        List<String> messageIds = new ArrayList<>();
        for(RabbitMetaMessage message : messages){
            /**如果消息超时，加入超时队列*/
            if(messageTimeOut(message.getMessageId())){
                messageAlert.add(message);
                messageIds.add(message.getMessageId());
            }
        }
        /**在redis中删除已超时的消息*/
        hashOperations.delete(MQConstants.MQ_MSG_READY, messageIds);
        return messageAlert;
    }

    @Override
    public Long incrResendKey(String key, String hashKey) {
        return  redisTemplate.opsForHash().increment(key, hashKey, 1);
    }

    @Override
    public Long getResendValue(String key, String hashKey) {
        return (Long) redisTemplate.opsForHash().get(key, hashKey);
    }

    boolean messageTimeOut(String messageId) throws Exception{
        String messageTime = (messageId.split(MQConstants.DB_SPLIT))[2];
        long timeGap = System.currentTimeMillis() -
                new SimpleDateFormat(MQConstants.TIME_PATTERN).parse(messageTime).getTime();
        if(timeGap > MQConstants.TIME_GAP){
            return true;
        }
        return false;
    }
}
