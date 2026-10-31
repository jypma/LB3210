package com.example.demoproject;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.vavr.jackson.datatype.VavrModule;

@Configuration
public class DemoConfig {
    @Bean
    public Function<org.jooq.Configuration,DemoRepository> repository() {
        return DemoRepository.Live::apply;
    }

    // To allow VAVR collections to be read/written to JSON using Jackson
    @Bean
    public VavrModule vavrJsonModule() {
        return new VavrModule();
    }
}
