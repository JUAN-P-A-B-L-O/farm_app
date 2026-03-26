package com.jpsoftware.farmapp.unit.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.mapper.AnimalMapper;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.animal.service.AnimalService;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class AnimalServiceTest {

    private InMemoryAnimalRepository repositoryHandler;
    private AnimalService animalService;
    private CreateAnimalRequest createAnimalRequest;
    private AnimalEntity animalEntity;

    @BeforeEach
    void setUp() {
        repositoryHandler = new InMemoryAnimalRepository();
        animalService = new AnimalService(repositoryHandler.createProxy(), new AnimalMapper());

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
        AnimalResponse response = animalService.create(createAnimalRequest);

        assertNotNull(response);
        assertEquals("TAG-001", response.getTag());
        assertEquals("Angus", response.getBreed());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("FARM-001", response.getFarmId());
        assertEquals(1, repositoryHandler.saveCalls);
    }

    @Test
    void shouldFailWhenTagAlreadyExists() {
        repositoryHandler.store(animalEntity);

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> animalService.create(createAnimalRequest));

        assertEquals("Animal with this tag already exists", exception.getMessage());
        assertEquals(0, repositoryHandler.saveCalls);
    }

    @Test
    void shouldFindAllAnimals() {
        repositoryHandler.store(animalEntity);

        List<AnimalResponse> responses = animalService.findAll(null);

        assertEquals(1, responses.size());
        assertEquals("animal-1", responses.get(0).getId());
        assertEquals("TAG-001", responses.get(0).getTag());
        assertEquals(1, repositoryHandler.findAllCalls);
    }

    @Test
    void shouldFindAnimalById() {
        repositoryHandler.store(animalEntity);

        AnimalResponse response = animalService.findById("animal-1");

        assertNotNull(response);
        assertEquals("animal-1", response.getId());
        assertEquals("TAG-001", response.getTag());
        assertEquals("animal-1", repositoryHandler.lastFindById);
    }

    @Test
    void shouldThrowWhenAnimalNotFound() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> animalService.findById("missing-id"));

        assertEquals("Animal not found", exception.getMessage());
        assertEquals("missing-id", repositoryHandler.lastFindById);
    }

    @Test
    void shouldUpdateAnimal() {
        UpdateAnimalRequest request = new UpdateAnimalRequest(
                "TAG-999",
                "Nelore",
                LocalDate.of(2021, 5, 20),
                "INACTIVE",
                "FARM-999");
        repositoryHandler.store(animalEntity);

        AnimalResponse response = animalService.update("animal-1", request);

        assertEquals("TAG-999", response.getTag());
        assertEquals("Nelore", response.getBreed());
        assertEquals(LocalDate.of(2021, 5, 20), response.getBirthDate());
        assertEquals("INACTIVE", response.getStatus());
        assertEquals("FARM-999", response.getFarmId());
        assertEquals("animal-1", repositoryHandler.lastFindById);
        assertEquals(1, repositoryHandler.saveCalls);
    }

    @Test
    void shouldDeleteAnimal() {
        repositoryHandler.store(animalEntity);

        animalService.delete("animal-1");

        assertEquals("animal-1", repositoryHandler.lastFindById);
        assertEquals("animal-1", repositoryHandler.lastDeletedId);
    }

    private static class InMemoryAnimalRepository {

        private final Map<String, AnimalEntity> data = new LinkedHashMap<>();
        private int sequence = 1;
        private int saveCalls;
        private int findAllCalls;
        private String lastFindById;
        private String lastDeletedId;

        AnimalRepository createProxy() {
            return (AnimalRepository) Proxy.newProxyInstance(
                    AnimalRepository.class.getClassLoader(),
                    new Class<?>[]{AnimalRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("save".equals(methodName)) {
                            saveCalls++;
                            AnimalEntity entity = (AnimalEntity) args[0];
                            if (entity.getId() == null) {
                                entity.setId("animal-" + sequence++);
                            }
                            data.put(entity.getId(), entity);
                            return entity;
                        }
                        if ("findAll".equals(methodName)) {
                            findAllCalls++;
                            return new ArrayList<>(data.values());
                        }
                        if ("findById".equals(methodName)) {
                            lastFindById = (String) args[0];
                            return Optional.ofNullable(data.get(args[0]));
                        }
                        if ("deleteById".equals(methodName)) {
                            lastDeletedId = (String) args[0];
                            data.remove(args[0]);
                            return null;
                        }
                        if ("findByFarmId".equals(methodName)) {
                            return data.values().stream()
                                    .filter(entity -> entity.getFarmId().equals(args[0]))
                                    .toList();
                        }
                        if ("existsByTag".equals(methodName)) {
                            return data.values().stream()
                                    .anyMatch(entity -> entity.getTag().equals(args[0]));
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return "InMemoryAnimalRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }

        void store(AnimalEntity entity) {
            data.put(entity.getId(), entity);
        }
    }
}
