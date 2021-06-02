package com.example.demoproject;

import java.util.function.Function;

import com.example.demoproject.chat.ChatRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfig {
    @Bean
    public Function<org.jooq.Configuration,DemoRepository> repository() {
        return DemoRepository.Live::apply;
    }
    @Bean
    public Function<org.jooq.Configuration, ChatRepository> demoRepository() {
        return ChatRepository.Live::apply;
    }
}
