package com.example.shop.order.catalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Component
public class CatalogClient {

    private final WebClient webClient;

    public CatalogClient(@Value("${catalog.base-url}") String baseUrl, WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public ProductDto getProduct(Long productId) {
        return webClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.createException().map(ex ->
                        new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Catalog error: " + ex.getMessage(), ex)))
                .bodyToMono(ProductDto.class)
                .block();
    }

    public void decreaseStock(Long productId, int quantity) {
        webClient.patch()
                .uri("/api/products/{id}/stock", productId)
                .bodyValue(Map.of("delta", -quantity))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.createException().map(ex ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock update failed: " + ex.getMessage(), ex)))
                .toBodilessEntity()
                .block();
    }
}
