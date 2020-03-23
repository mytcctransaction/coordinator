package com.mymq.controller;


import com.mymq.provider.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MessageProviderController {

 
  /** test if can get value from config center **/
//  @Value("${spring.rabbitmq.host}")
//  private String rabbitmqAddress;

  @Autowired
  MessageSender messageSender;


//  @Autowired
//  private RabbitSender rabbitSender;

  Logger logger = LoggerFactory.getLogger(getClass());


  @GetMapping("test_mq_message")
  public String testMessageWithAnnotation() throws Exception {
        messageSender.transSend();
		return "发送成功！";
  }



}
