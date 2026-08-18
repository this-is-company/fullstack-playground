package com.example.enumapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EnumMybatisValidationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnumMybatisValidationApplication.class, args);
    }
}
