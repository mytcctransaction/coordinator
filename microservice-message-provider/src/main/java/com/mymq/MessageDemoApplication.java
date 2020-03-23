package com.mymq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;



@SpringBootApplication(scanBasePackages = {"com.mymq.amqp","com.mymq.*","com.mymq.amqp.daemontask"})
@Configuration
public class MessageDemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(MessageDemoApplication.class, args);
  }
}
