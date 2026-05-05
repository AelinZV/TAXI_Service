package com.taxi.tripservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TripServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripServiceApplication.class, args);
    }

    /**
     * RestTemplate для межсервисных вызовов.
     *
     * Для продакшена рекомендуется добавить:
     * - Custom ErrorHandler для обработки 4xx/5xx ответов
     * - ClientHttpRequestInterceptor для добавления service-to-service токенов
     * - Настройку таймаутов (connectionTimeout, readTimeout)
     * - Поддержку retry через Spring Retry
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}