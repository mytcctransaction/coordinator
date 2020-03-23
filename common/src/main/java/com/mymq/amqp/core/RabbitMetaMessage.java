package com.mymq.amqp.core;

import lombok.Getter;
import lombok.Setter;

/**
 * <p><b>Description:</b> 常量类 <p>
 * <b>Company:</b> 
 *
 * @version V0.1
 */
@Setter
@Getter
public class RabbitMetaMessage {
	//消息id
	String messageId;
	//交换机名称
	String exchange;
	//路由key
	String routingKey;
	//消息体
	Object payload;
	//日期
	String date;
	//主业务表id
    Object bizId;
    //发送状态1：已成功发送 空值：未成功发送
    int sendStatus;
	//消费状态1：已成功消费 空值：未成功消费
    int consumeStatus;

	String dbCoordinator;

	public String getMessageId(){
		return this.messageId;
	}
	public void setMessageId(String messageId){
		this.messageId = messageId;
	}
	public Object getPayload() {
		return payload;
	}
	public void setPayload(Object payload) {
		this.payload = payload;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public String getRoutingKey() {
		return routingKey;
	}
	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}


}
