package com.example.shop.order.web;

import com.example.shop.order.domain.ShopOrder;
import com.example.shop.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final Environment environment;

    public OrderController(OrderService orderService, Environment environment) {
        this.orderService = orderService;
        this.environment = environment;
    }

    @PostMapping
    public ShopOrder create(@Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
        String username = resolveUsername(authentication);
        return orderService.create(username, request);
    }

    @GetMapping("/me")
    public List<ShopOrder> mine(Authentication authentication) {
        String username = resolveUsername(authentication);
        return orderService.findMine(username);
    }

    @GetMapping
    public List<ShopOrder> listAll(Authentication authentication) {
        if (isDevProfile()) {
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
            }
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (!admin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN role required");
            }
        }
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ShopOrder get(@PathVariable Long id) {
        return orderService.findById(id);
    }

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    private String resolveUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            // local profile: permitAll for swagger try-out
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String preferred = jwt.getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
            return jwt.getSubject();
        }
        return authentication.getName();
    }
}
