package com.mymq.amqp.sender;

import com.mymq.amqp.annotation.TransMQMessage;
import com.mymq.amqp.resp.MQMessageResp;
import com.mymq.amqp.core.DBCoordinator;
import com.mymq.amqp.config.MQConstants;
import com.mymq.amqp.core.RabbitMetaMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/** 
 * 描述：封装sender
 *
 * 创建时间：2017年10月14日 下午10:30:00 
 * @version 1.0.0 
 */  
@Component 
@Aspect 
public class MQTransactionSender {
	Logger logger = LoggerFactory.getLogger(MQTransactionSender.class);
	
	@Autowired
	RabbitMQSender rabbitSender;

	@Autowired
    DBCoordinator dbCoordinator;

	@Autowired
	ApplicationContext applicationContext;

	/**  定义注解类型的切点，只要方法上有该注解，都会匹配  */
    @Pointcut("@annotation(com.mymq.amqp.annotation.TransMQMessage)")
    public void annotationSender(){          
    }  
    
    @Around("annotationSender()&& @annotation(rd)")
    public void sendMsg(ProceedingJoinPoint joinPoint, TransMQMessage rd) throws Throwable {
		logger.info("==> custom mq annotation,rd"+ rd);
    	String exchange = rd.exchange();
    	String bindingKey = rd.bindingKey();
    	String dbCoordinator = rd.dbCoordinator();
		final String msgId = UUID.randomUUID().toString();
		String date= getCurrentDateTime();
    	String bizName = rd.bizName() +MQConstants.DB_SPLIT+msgId+ MQConstants.DB_SPLIT +date;
    	DBCoordinator coordinator = null;
    	try{
    		coordinator = (DBCoordinator) applicationContext.getBean(dbCoordinator);
		}catch (Exception ex){
			logger.error("无消息存储类，事务执行终止");
			return;
    	}

		/**发送前暂存消息*/
    	coordinator.setMsgPrepare(bizName);

		Object returnObj = null;
    	/** 执行业务函数 */
    	try{
			returnObj = joinPoint.proceed();
		}catch (Exception ex){
    		//TODO 失败要删除prepare消息
    		logger.error("业务执行失败,业务名称:" + bizName);
			coordinator.delMsgPrepare(bizName);
    		throw ex;
		}
		MQMessageResp mqMessageResp=null;
		if(returnObj == null) {
			mqMessageResp=MQMessageResp.builder().message(MQConstants.BLANK_STR).build();
		}else {
			mqMessageResp=(MQMessageResp)returnObj;
		}

		/** 生成一个发送对象 */
		RabbitMetaMessage rabbitMetaMessage = new RabbitMetaMessage();
		rabbitMetaMessage.setMessageId(bizName);
		rabbitMetaMessage.setBizId(mqMessageResp.getBizId());
		rabbitMetaMessage.setDbCoordinator(dbCoordinator);
		/**设置交换机 */
		rabbitMetaMessage.setExchange(exchange);
		/**指定routing key */
		rabbitMetaMessage.setRoutingKey(bindingKey);
		/** 设置需要传递的消息体,可以是任意对象 */
		rabbitMetaMessage.setPayload(mqMessageResp==null?"":mqMessageResp.getMessage());
		rabbitMetaMessage.setDate(date);
		/** 将消息设置为ready状态*/
		coordinator.setMsgReady(bizName, rabbitMetaMessage);
		
		/** 发送消息 */
		try {
			rabbitSender.setCorrelationData(dbCoordinator,bizName);
			rabbitSender.send(rabbitMetaMessage);
		} catch (Exception e) {
			logger.error("第一阶段消息发送异常" + bizName + e);
			throw e;
		}
   }

   public static String getCurrentDateTime(){
	   SimpleDateFormat df = new SimpleDateFormat(MQConstants.TIME_PATTERN);
	   return df.format(new Date());
   }
}
