package com.mymq.amqp.config;//package com.coolmq.amqp.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * <p><b>Description:</b> RabbitMQ交换机、队列的配置类.定义交换机、key、queue并做好绑定。
 * <p><b>Company:</b>
 *
 * @version V0.1
 */
@Configuration
public class BizQueueConfig {


    //========================== 声明交换机 ==========================
    /**
     * 交换机
     */
    @Bean
    public DirectExchange transExchange() {
        return new DirectExchange("exchange.transmsg");
    }



    //========================== 声明队列 ===========================
    /**
     * 队列
     */
    @Bean
    public Queue transQueue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("x-dead-letter-exchange", MQConstants.DLX_EXCHANGE);//设置死信交换机
        map.put("x-dead-letter-routing-key", MQConstants.DLX_ROUTING_KEY);//设置死信routingKey
        //map.put("x-message-ttl", 8000);
        return new Queue("queue.transmsg",true,false,false,map);
    }
    /**
     * 通过路由key绑定交换机队列
     */
    @Bean
    public Binding transBinding() {
        return BindingBuilder.bind(transQueue()).to(transExchange())
                .with("key.transmsg");
    }

    /**
     * 队列的监听
     * @param connectionFactory RabbitMQ连接工厂
     * @param transMessageListener  队列监听器
     * @return 监听容器对象
     */
//    @Bean
//    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory,
//    		TransMessageListener transMessageListener) {
//
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//        container.setQueues(transQueue());
//        container.setExposeListenerChannel(true);
//        container.setMessageConverter(new Jackson2JsonMessageConverter());
//        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        container.setMessageListener(transMessageListener);
//
//        return container;
//    }



}
