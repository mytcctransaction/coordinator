package com.mymq.amqp.annotation;

import java.lang.annotation.*;

/**
 * 事务注解类，用来无侵入的实现分布式事务
 * */
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)
public @interface TransMQMessage {
	String exchange() default "";   //要发送的交换机
	String bindingKey() default "";    //要发送的key
	String bizName() default "";      //业务编号
	String dbCoordinator() default "";	//消息协调的处理方式db or redis
}  
