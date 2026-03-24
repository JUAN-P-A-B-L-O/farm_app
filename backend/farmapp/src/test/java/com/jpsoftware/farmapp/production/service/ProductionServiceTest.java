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
import com.jpsoftware.farmapp.production.dto.ProductionSummaryResponse;
import com.jpsoftware.farmapp.production.dto.UpdateProductionRequest;
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
    void shouldReturnTotalProduction() {
        when(animalRepository.existsById("animal-1")).thenReturn(true);
        when(productionRepository.sumQuantityByAnimalId("animal-1")).thenReturn(35.5);

        ProductionSummaryResponse response = productionService.getSummaryByAnimal("animal-1");

        assertNotNull(response);
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(35.5, response.getTotalQuantity());
        verify(animalRepository).existsById("animal-1");
        verify(productionRepository).sumQuantityByAnimalId("animal-1");
    }

    @Test
    void shouldReturnZeroWhenNoProduction() {
        when(animalRepository.existsById("animal-1")).thenReturn(true);
        when(productionRepository.sumQuantityByAnimalId("animal-1")).thenReturn(null);

        ProductionSummaryResponse response = productionService.getSummaryByAnimal("animal-1");

        assertNotNull(response);
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(0.0, response.getTotalQuantity());
        verify(animalRepository).existsById("animal-1");
        verify(productionRepository).sumQuantityByAnimalId("animal-1");
    }

    @Test
    void shouldFailWhenAnimalNotFound() {
        when(animalRepository.existsById("animal-1")).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.getSummaryByAnimal("animal-1"));

        assertEquals("Animal not found", exception.getMessage());
    }

    @Test
    void shouldFailWhenAnimalIdIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productionService.getSummaryByAnimal(""));

        assertEquals("animalId must not be blank", exception.getMessage());
    }

    @Test
    void shouldFilterByAnimalId() {
        when(productionRepository.findByAnimalId("animal-1")).thenReturn(List.of(productionEntity));

        List<ProductionResponse> responses = productionService.findAll("animal-1", null);

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
        verify(productionRepository).findByAnimalId("animal-1");
    }

    @Test
    void shouldFilterByDate() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        when(productionRepository.findByDate(date)).thenReturn(List.of(productionEntity));

        List<ProductionResponse> responses = productionService.findAll(null, date);

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals(date, responses.get(0).getDate());
        verify(productionRepository).findByDate(date);
    }

    @Test
    void shouldFilterByAnimalIdAndDate() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        when(productionRepository.findByAnimalIdAndDate("animal-1", date)).thenReturn(List.of(productionEntity));

        List<ProductionResponse> responses = productionService.findAll("animal-1", date);

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
        assertEquals(date, responses.get(0).getDate());
        verify(productionRepository).findByAnimalIdAndDate("animal-1", date);
    }

    @Test
    void shouldReturnAllWhenNoFilters() {
        when(productionRepository.findAll()).thenReturn(List.of(productionEntity));

        List<ProductionResponse> responses = productionService.findAll(null, null);

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

    @Test
    void shouldUpdateProduction() {
        UpdateProductionRequest request = new UpdateProductionRequest(LocalDate.of(2026, 3, 21), 15.0);
        when(productionRepository.findById("production-1")).thenReturn(java.util.Optional.of(productionEntity));
        when(productionRepository.save(any(ProductionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductionResponse response = productionService.update("production-1", request);

        assertNotNull(response);
        assertEquals("production-1", response.getId());
        assertEquals(LocalDate.of(2026, 3, 21), response.getDate());
        assertEquals(15.0, response.getQuantity());
        verify(productionRepository).findById("production-1");
        verify(productionRepository).save(productionEntity);
    }

    @Test
    void shouldFailWhenQuantityInvalid() {
        UpdateProductionRequest request = new UpdateProductionRequest(null, 0.0);
        when(productionRepository.findById("production-1")).thenReturn(java.util.Optional.of(productionEntity));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productionService.update("production-1", request));

        assertEquals("quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldFailWhenProductionNotFound() {
        UpdateProductionRequest request = new UpdateProductionRequest(LocalDate.of(2026, 3, 21), 15.0);
        when(productionRepository.findById("missing-id")).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.update("missing-id", request));

        assertEquals("Production not found", exception.getMessage());
    }
}
