package com.example.demoproject;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DemoQueues {
    private final RabbitTemplate template;

    public DemoQueues(RabbitTemplate template) {
        this.template = template;
    }

    public void sendDemoQueue(String message) {
        template.invoke(cb -> {
            cb.convertAndSend("demoQueue", message);
            // Potentially we can do other things in between sending the message and awaiting
            // its conformation.
            cb.waitForConfirmsOrDie(1000);

            // RabbitTemplate.invoke method is declared to take a closure which must return a value,
            // even though that closure is intended to yield side effects (publishing on a queue).
            // Hence, we unfortunately need a dummy return statement here.
            return true;
        });
    }
}
