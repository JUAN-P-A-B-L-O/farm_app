package com.jpsoftware.farmapp.shared.config;

import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class DefaultAdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdminInitializer.class);

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DefaultAdminInitializer(
            UserRepository userRepository,
            FarmRepository farmRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_EMAIL:admin@farmapp.com}") String adminEmail,
            @Value("${ADMIN_PASSWORD:admin123}") String adminPassword) {
        this.userRepository = userRepository;
        this.farmRepository = farmRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        UserEntity admin = new UserEntity();
        admin.setName("Default Admin");
        admin.setEmail(adminEmail);
        admin.setRole("MANAGER");
        admin.setPassword(passwordEncoder.encode(adminPassword));

        userRepository.save(admin);
        farmRepository.save(new FarmEntity(null, "Default Farm", admin.getId()));
        logger.info("Default admin user created");
    }
}
