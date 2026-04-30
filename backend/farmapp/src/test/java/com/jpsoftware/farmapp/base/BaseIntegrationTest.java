package com.jpsoftware.farmapp.base;

import com.jpsoftware.farmapp.auth.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.animalbatch.repository.AnimalBatchMemberRepository;
import com.jpsoftware.farmapp.animalbatch.repository.AnimalBatchRepository;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AnimalRepository animalRepository;

    @Autowired
    protected AnimalBatchRepository animalBatchRepository;

    @Autowired
    protected AnimalBatchMemberRepository animalBatchMemberRepository;

    @Autowired
    protected ProductionRepository productionRepository;

    @Autowired
    protected FeedingRepository feedingRepository;

    @Autowired
    protected FeedTypeRepository feedTypeRepository;

    @Autowired
    protected FarmRepository farmRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserFarmAssignmentRepository userFarmAssignmentRepository;

    @Autowired
    protected TokenService tokenService;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void resetDatabase() {
        productionRepository.deleteAll();
        feedingRepository.deleteAll();
        animalBatchMemberRepository.deleteAll();
        animalBatchRepository.deleteAll();
        animalRepository.deleteAll();
        feedTypeRepository.deleteAll();
        userFarmAssignmentRepository.deleteAll();
        farmRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected UserEntity createAuthenticatedUser() {
        return createAuthenticatedUser("MANAGER");
    }

    protected UserEntity createAuthenticatedUser(String role) {
        UserEntity user = new UserEntity();
        user.setName("Jane Doe");
        user.setEmail(role.toLowerCase() + "-" + UUID.randomUUID() + "@farm.com");
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("farmapp@123"));
        user.setActive(true);
        return userRepository.save(user);
    }

    protected String bearerToken(UserEntity user) {
        return "Bearer " + tokenService.generateToken(user);
    }

    protected FarmEntity createFarmOwnedBy(UserEntity owner, String name) {
        FarmEntity farm = new FarmEntity();
        farm.setName(name);
        farm.setOwnerId(owner.getId());
        return farmRepository.save(farm);
    }
}
