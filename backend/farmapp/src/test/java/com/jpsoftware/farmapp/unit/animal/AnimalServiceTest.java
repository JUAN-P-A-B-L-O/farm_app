package com.jpsoftware.farmapp.unit.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.SellAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.mapper.AnimalMapper;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.animal.service.AnimalService;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AnimalServiceTest {

    private final AnimalRepository animalRepository = org.mockito.Mockito.mock(AnimalRepository.class);
    private final FarmAccessService farmAccessService = org.mockito.Mockito.mock(FarmAccessService.class);
    private AnimalService animalService;

    @BeforeEach
    void setUp() {
        animalService = new AnimalService(animalRepository, new AnimalMapper(), farmAccessService);
    }

    @Test
    void shouldSellActiveAnimal() {
        AnimalEntity animalEntity = AnimalEntity.builder()
                .id("animal-1")
                .tag("TAG-001")
                .breed("Angus")
                .birthDate(LocalDate.of(2022, 1, 10))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId("farm-1")
                .build();
        SellAnimalRequest request = new SellAnimalRequest(3200.0, LocalDate.of(2026, 4, 14));

        when(animalRepository.findByIdAndFarmId("animal-1", "farm-1")).thenReturn(Optional.of(animalEntity));
        when(animalRepository.save(any(AnimalEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnimalResponse response = animalService.sell("animal-1", request, "farm-1");

        ArgumentCaptor<AnimalEntity> animalCaptor = ArgumentCaptor.forClass(AnimalEntity.class);
        verify(animalRepository).save(animalCaptor.capture());
        verify(farmAccessService).validateAccessibleFarmIfPresent("farm-1");

        AnimalEntity savedAnimal = animalCaptor.getValue();
        assertEquals(AnimalEntity.STATUS_SOLD, savedAnimal.getStatus());
        assertEquals(3200.0, savedAnimal.getSalePrice());
        assertEquals(LocalDate.of(2026, 4, 14), savedAnimal.getSaleDate());
        assertEquals(AnimalEntity.STATUS_SOLD, response.getStatus());
        assertEquals(3200.0, response.getSalePrice());
        assertEquals(LocalDate.of(2026, 4, 14), response.getSaleDate());
    }

    @Test
    void shouldRejectSellingAlreadySoldAnimal() {
        AnimalEntity animalEntity = AnimalEntity.builder()
                .id("animal-1")
                .status(AnimalEntity.STATUS_SOLD)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId("farm-1")
                .build();

        when(animalRepository.findByIdAndFarmId("animal-1", "farm-1")).thenReturn(Optional.of(animalEntity));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> animalService.sell("animal-1", new SellAnimalRequest(1000.0, null), "farm-1"));

        assertEquals("Animal is already sold", exception.getMessage());
        verify(animalRepository, never()).save(any(AnimalEntity.class));
    }

    @Test
    void shouldRejectSoldTransitionThroughGenericUpdate() {
        AnimalEntity animalEntity = AnimalEntity.builder()
                .id("animal-1")
                .tag("TAG-001")
                .breed("Angus")
                .birthDate(LocalDate.of(2022, 1, 10))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId("farm-1")
                .build();
        UpdateAnimalRequest request = new UpdateAnimalRequest();
        request.setStatus(AnimalEntity.STATUS_SOLD);

        when(animalRepository.findByIdAndFarmId("animal-1", "farm-1")).thenReturn(Optional.of(animalEntity));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> animalService.update("animal-1", request, "farm-1"));

        assertEquals("Use the sell action to mark an animal as SOLD", exception.getMessage());
        verify(animalRepository, never()).save(any(AnimalEntity.class));
        verify(farmAccessService).validateAccessibleFarmIfPresent(eq("farm-1"));
    }

    @Test
    void shouldReturnPaginatedAnimals() {
        AnimalEntity animalEntity = AnimalEntity.builder()
                .id("animal-1")
                .tag("TAG-001")
                .breed("Angus")
                .birthDate(LocalDate.of(2022, 1, 10))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId("farm-1")
                .build();
        when(animalRepository.findByFarmId(eq("farm-1"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(animalEntity), PageRequest.of(0, 10), 1));

        PaginatedResponse<AnimalResponse> response = animalService.findAllPaginated("farm-1", 0, 10);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }
}
