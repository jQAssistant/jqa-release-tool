package com.buschmais.jqassistant.release.core;

import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Bean
    public ExitCodeExceptionMapper exitCodeExceptionMapper() {
        return new RPExitCodeExceptionMapper();
    }
}
