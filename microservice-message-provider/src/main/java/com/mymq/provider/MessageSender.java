package com.mymq.provider;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

//import com.coolmq.amqp.annotation.TransMessage;
import com.mymq.amqp.annotation.TransMQMessage;
import com.mymq.amqp.resp.MQMessageResp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageSender {
	
	@Autowired
	private AmqpTemplate rabbitTemplate;
	
	public void send() {
		String context ="hello" + new Date();
		this.rabbitTemplate.convertAndSend("hello", context);
	}


	@TransMQMessage(exchange = "exchange.transmsg",bindingKey = "key.transmsg", bizName = "transSend",
	   dbCoordinator = "redis")
	public MQMessageResp transSend(){
		log.info("..........do business...........");
		log.info("..........构建MQ对象...........");
		Object bizId=UUID.randomUUID().toString();
		MQMessageResp resp=MQMessageResp.builder().message("hello world"+ (new Random()).nextInt()).bizId(bizId).build();
		log.info("..........返回MQ对象...........");
		return resp;
	}
}
