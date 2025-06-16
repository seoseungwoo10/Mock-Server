package com.example.mockserver.config;

import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

/**
 * 애플리케이션에서 사용될 Bean들을 설정하는 클래스입니다.
 */
@Configuration
public class AppConfig {

    /**
     * 외부 API 호출(Relay, Webhook 등)에 사용될 RestTemplate Bean을 생성합니다.
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 현실적인 가짜 데이터를 생성하기 위한 Faker Bean을 생성합니다.
     * 한국어 데이터를 생성하도록 Locale을 설정합니다.
     * @return Faker 인스턴스
     */
    @Bean
    public Faker faker() {
        return new Faker(new Locale("ko"));
    }
}
