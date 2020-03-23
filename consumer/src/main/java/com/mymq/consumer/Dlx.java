package com.mymq.consumer;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

public class Dlx {



        public static void main(String[] args) throws Exception {

            ConnectionFactory factory = new ConnectionFactory();

            factory.setHost("192.168.1.233");
            factory.setPort(5672);
            factory.setUsername("admin");
            factory.setPassword("admin");
            factory.setVirtualHost("my_vhost");

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // DLX
            channel.exchangeDeclare("exchange.dlx", "direct", true);
            channel.exchangeDeclare("exchange.normal", "fanout", true);

            Map<String, Object> arg = new HashMap<String, Object>();
            // 设置DLX
            arg.put("x-dead-letter-exchange", "exchange.dlx");
            arg.put("x-dead-letter-routing-key", "routingkey.dlx");
            // 设置消息过期时间，消息过期后，会重新发布到DLX
            arg.put("x-message-ttl", 5000);
            channel.queueDeclare("queue.normal", true, false, false, arg);
            //	死信队列
            channel.queueDeclare("queue.dlx", true, false, false, null);

            channel.queueBind("queue.normal", "exchange.normal", "");
            channel.queueBind("queue.dlx", "exchange.dlx", "routingkey.dlx");

            channel.close();
            connection.close();
        }


}


