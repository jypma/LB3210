package com.example.demoproject;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DemoSpringListener {

    /**
     * When this method returns, an "ack" is sent to RabbitMQ confirming
     * delivery, unless an exception occurs. According to the documentation:
     * https://docs.spring.io/spring-amqp/reference/html/#exception-handling
     */
    @RabbitListener(queues = "demoQueue")
    public void handle(String message) {
        System.out.println("We've got a message: [" + message + "]");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
