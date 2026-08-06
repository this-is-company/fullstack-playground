package com.example.securitysso.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        model.addAttribute("authenticated", authentication != null && authentication.isAuthenticated());
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            model.addAttribute("username", oidcUser.getPreferredUsername());
            model.addAttribute("claims", oidcUser.getClaims());
        }
        return "index";
    }

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            model.addAttribute("username", oidcUser.getPreferredUsername());
            model.addAttribute("email", oidcUser.getEmail());
            model.addAttribute("authorities", authentication.getAuthorities());
        } else if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            model.addAttribute("username", jwt.getClaimAsString("preferred_username"));
            model.addAttribute("email", jwt.getClaimAsString("email"));
            model.addAttribute("authorities", authentication.getAuthorities());
        }
        return "home";
    }
}
