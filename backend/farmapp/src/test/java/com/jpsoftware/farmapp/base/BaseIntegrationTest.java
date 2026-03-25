package com.jpsoftware.farmapp.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    protected ProductionRepository productionRepository;

    @Autowired
    protected FeedingRepository feedingRepository;

    @Autowired
    protected FeedTypeRepository feedTypeRepository;

    @Autowired
    protected UserRepository userRepository;

    @BeforeEach
    void resetDatabase() {
        productionRepository.deleteAll();
        feedingRepository.deleteAll();
        animalRepository.deleteAll();
        feedTypeRepository.deleteAll();
        userRepository.deleteAll();
    }
}
