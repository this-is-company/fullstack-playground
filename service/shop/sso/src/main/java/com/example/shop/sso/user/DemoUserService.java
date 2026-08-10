package com.example.shop.sso.user;

import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DemoUserService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, DemoUser> users = new ConcurrentHashMap<>();

    public DemoUserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void seed() {
        users.put("admin", new DemoUser("admin", passwordEncoder.encode("admin123"), List.of("ADMIN", "USER")));
        users.put("shopper", new DemoUser("shopper", passwordEncoder.encode("shopper123"), List.of("USER")));
    }

    public Optional<DemoUser> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public boolean matches(DemoUser user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.passwordHash());
    }
}
