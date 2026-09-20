package com.smartclinic.config;

import com.smartclinic.model.Admin;
import com.smartclinic.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Makes sure a default admin exists so a fresh install can be logged into. */
@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner createDefaultAdmin(AdminRepository adminRepository, PasswordEncoder encoder) {
        return args -> {
            if (adminRepository.count() == 0) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("Admin@123"));
                adminRepository.save(admin);
            }
        };
    }
}
