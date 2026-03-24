package com.jpsoftware.farmapp.production.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock
    private ProductionRepository productionRepository;

    @InjectMocks
    private ProductionService productionService;

    private CreateProductionRequest createProductionRequest;

    @BeforeEach
    void setUp() {
        createProductionRequest = new CreateProductionRequest(
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5);
    }

    @Test
    void shouldCreateProduction() {
        when(productionRepository.save(any(ProductionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductionResponse response = productionService.create(createProductionRequest);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(LocalDate.of(2026, 3, 20), response.getDate());
        assertEquals(12.5, response.getQuantity());
        verify(productionRepository).save(any(ProductionEntity.class));
    }

    @Test
    void shouldFailWhenQuantityIsInvalid() {
        CreateProductionRequest invalidRequest = new CreateProductionRequest(
                "animal-1",
                LocalDate.of(2026, 3, 20),
                0.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productionService.create(invalidRequest));

        assertEquals("Production quantity must be greater than zero", exception.getMessage());
    }
}
