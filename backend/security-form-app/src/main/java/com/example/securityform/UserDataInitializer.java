package com.example.securityform;

import com.example.securityform.user.AppUser;
import com.example.securityform.user.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserDataInitializer {

    @Bean
    CommandLineRunner initUsers(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.save(new AppUser("admin", passwordEncoder.encode("admin123"), "ADMIN"));
            repository.save(new AppUser("user", passwordEncoder.encode("user123"), "USER"));
        };
    }
}
