package com.jpsoftware.farmapp.production.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.mapper.ProductionMapper;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock
    private ProductionRepository productionRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Spy
    private ProductionMapper productionMapper;

    @InjectMocks
    private ProductionService productionService;

    private CreateProductionRequest createProductionRequest;
    private ProductionEntity productionEntity;

    @BeforeEach
    void setUp() {
        createProductionRequest = new CreateProductionRequest(
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5);

        productionEntity = new ProductionEntity();
        productionEntity.setId("production-1");
        productionEntity.setAnimalId("animal-1");
        productionEntity.setDate(LocalDate.of(2026, 3, 20));
        productionEntity.setQuantity(12.5);
    }

    @Test
    void shouldCreateProduction() {
        when(animalRepository.existsById(any())).thenReturn(true);
        when(productionRepository.save(any(ProductionEntity.class)))
                .thenAnswer(invocation -> {
                    ProductionEntity savedEntity = invocation.getArgument(0);
                    savedEntity.setId("production-1");
                    return savedEntity;
                });

        ProductionResponse response = productionService.create(createProductionRequest);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(LocalDate.of(2026, 3, 20), response.getDate());
        assertEquals(12.5, response.getQuantity());
        verify(productionRepository).save(any(ProductionEntity.class));
    }

    @Test
    void shouldFailWhenAnimalDoesNotExist() {
        when(animalRepository.existsById(any())).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.create(createProductionRequest));

        assertEquals("Animal not found", exception.getMessage());
    }

    @Test
    void shouldReturnAllProductions() {
        when(productionRepository.findAll()).thenReturn(List.of(productionEntity));

        List<ProductionResponse> responses = productionService.findAll();

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
        assertEquals(LocalDate.of(2026, 3, 20), responses.get(0).getDate());
        assertEquals(12.5, responses.get(0).getQuantity());
        verify(productionRepository).findAll();
    }

    @Test
    void shouldFailWhenQuantityIsInvalid() {
        CreateProductionRequest invalidRequest = new CreateProductionRequest(
                "animal-1",
                LocalDate.of(2026, 3, 20),
                0.0);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productionService.create(invalidRequest));

        assertEquals("quantity must be greater than zero", exception.getMessage());
    }
}
