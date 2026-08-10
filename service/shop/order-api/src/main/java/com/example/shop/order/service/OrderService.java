package com.example.shop.order.service;

import com.example.shop.order.catalog.CatalogClient;
import com.example.shop.order.catalog.ProductDto;
import com.example.shop.order.domain.OrderItem;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.OrderStatus;
import com.example.shop.order.domain.ShopOrder;
import com.example.shop.order.web.CreateOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    public OrderService(OrderRepository orderRepository, CatalogClient catalogClient) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public ShopOrder create(String username, CreateOrderRequest request) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        List<OrderItem> items = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest line : request.items()) {
            ProductDto product = catalogClient.getProduct(line.productId());
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + line.productId());
            }
            if (!product.active()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product inactive: " + line.productId());
            }
            if (product.stock() < line.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product " + line.productId());
            }
            catalogClient.decreaseStock(line.productId(), line.quantity());
            items.add(new OrderItem(product.id(), product.name(), line.quantity(), product.price()));
        }

        ShopOrder order = new ShopOrder();
        order.setUsername(username);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());
        order.setItems(items);
        return orderRepository.save(order);
    }

    public List<ShopOrder> findMine(String username) {
        return orderRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public List<ShopOrder> findAll() {
        return orderRepository.findAll();
    }

    public ShopOrder findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
