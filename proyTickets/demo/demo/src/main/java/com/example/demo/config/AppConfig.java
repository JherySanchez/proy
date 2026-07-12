package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // Esto nos permite inyectar un "navegador interno" en nuestros servicios
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
