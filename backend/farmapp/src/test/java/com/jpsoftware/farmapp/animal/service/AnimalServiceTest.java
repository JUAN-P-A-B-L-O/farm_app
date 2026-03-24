package com.jpsoftware.farmapp.animal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.mapper.AnimalMapper;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Spy
    private AnimalMapper animalMapper;

    @InjectMocks
    private AnimalService animalService;

    private CreateAnimalRequest createAnimalRequest;
    private AnimalEntity animalEntity;

    @BeforeEach
    void setUp() {
        createAnimalRequest = new CreateAnimalRequest(
                "TAG-001",
                "Angus",
                LocalDate.of(2022, 1, 10),
                "FARM-001");

        animalEntity = AnimalEntity.builder()
                .id("animal-1")
                .tag("TAG-001")
                .breed("Angus")
                .birthDate(LocalDate.of(2022, 1, 10))
                .status("ACTIVE")
                .farmId("FARM-001")
                .build();
    }

    @Test
    void shouldCreateAnimal() {
        when(animalRepository.save(any(AnimalEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnimalResponse response = animalService.create(createAnimalRequest);

        assertNotNull(response);
        assertEquals("TAG-001", response.getTag());
        assertEquals("Angus", response.getBreed());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("FARM-001", response.getFarmId());
        verify(animalRepository).save(any(AnimalEntity.class));
    }

    @Test
    void shouldFindAllAnimals() {
        when(animalRepository.findAll()).thenReturn(List.of(animalEntity));

        List<AnimalResponse> responses = animalService.findAll(null);

        assertEquals(1, responses.size());
        assertEquals("animal-1", responses.get(0).getId());
        assertEquals("TAG-001", responses.get(0).getTag());
        verify(animalRepository).findAll();
    }

    @Test
    void shouldFindAnimalById() {
        when(animalRepository.findById("animal-1")).thenReturn(Optional.of(animalEntity));

        AnimalResponse response = animalService.findById("animal-1");

        assertNotNull(response);
        assertEquals("animal-1", response.getId());
        assertEquals("TAG-001", response.getTag());
        verify(animalRepository).findById("animal-1");
    }

    @Test
    void shouldThrowWhenAnimalNotFound() {
        when(animalRepository.findById("missing-id")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> animalService.findById("missing-id"));

        assertEquals("Animal not found", exception.getMessage());
        verify(animalRepository).findById("missing-id");
    }

    @Test
    void shouldUpdateAnimal() {
        UpdateAnimalRequest request = new UpdateAnimalRequest(
                "TAG-999",
                "Nelore",
                LocalDate.of(2021, 5, 20),
                "INACTIVE",
                "FARM-999");

        when(animalRepository.findById("animal-1")).thenReturn(Optional.of(animalEntity));
        when(animalRepository.save(any(AnimalEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnimalResponse response = animalService.update("animal-1", request);

        assertEquals("TAG-999", response.getTag());
        assertEquals("Nelore", response.getBreed());
        assertEquals(LocalDate.of(2021, 5, 20), response.getBirthDate());
        assertEquals("INACTIVE", response.getStatus());
        assertEquals("FARM-999", response.getFarmId());
        verify(animalRepository).findById("animal-1");
        verify(animalRepository).save(animalEntity);
    }

    @Test
    void shouldDeleteAnimal() {
        when(animalRepository.findById("animal-1")).thenReturn(Optional.of(animalEntity));

        animalService.delete("animal-1");

        verify(animalRepository).findById("animal-1");
        verify(animalRepository, times(1)).deleteById("animal-1");
    }
}
