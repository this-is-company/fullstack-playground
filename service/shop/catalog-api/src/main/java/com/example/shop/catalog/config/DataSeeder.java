package com.example.shop.catalog.config;

import com.example.shop.catalog.domain.Product;
import com.example.shop.catalog.domain.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.saveAll(List.of(
                    new Product("Wireless Mouse", "Ergonomic wireless mouse", new BigDecimal("29.99"), 100, true),
                    new Product("Mechanical Keyboard", "RGB mechanical keyboard", new BigDecimal("89.00"), 50, true),
                    new Product("USB-C Hub", "7-in-1 USB-C hub", new BigDecimal("45.50"), 75, true),
                    new Product("Monitor Stand", "Adjustable aluminum stand", new BigDecimal("39.99"), 40, true),
                    new Product("Webcam HD", "1080p webcam with mic", new BigDecimal("59.00"), 30, true)
            ));
        };
    }
}
