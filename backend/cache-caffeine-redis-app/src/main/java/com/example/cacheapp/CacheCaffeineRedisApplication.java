package com.example.cacheapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@MapperScan("com.example.cacheapp.product")
public class CacheCaffeineRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheCaffeineRedisApplication.class, args);
    }
}
