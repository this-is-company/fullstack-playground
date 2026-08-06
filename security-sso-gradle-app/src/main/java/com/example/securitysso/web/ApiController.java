package com.example.securitysso.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/public/hello")
    public Map<String, String> publicHello() {
        return Map.of("message", "public ok");
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", authentication.getName());
        body.put("authorities", authentication.getAuthorities());

        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            body.put("type", "oidc");
            body.put("username", oidcUser.getPreferredUsername());
            body.put("email", oidcUser.getEmail());
        } else if (principal instanceof Jwt jwt) {
            body.put("type", "jwt");
            body.put("username", jwt.getClaimAsString("preferred_username"));
            body.put("email", jwt.getClaimAsString("email"));
            body.put("subject", jwt.getSubject());
        }
        return body;
    }

    @GetMapping("/admin/ping")
    public Map<String, String> adminPing() {
        return Map.of("message", "admin ok");
    }
}
