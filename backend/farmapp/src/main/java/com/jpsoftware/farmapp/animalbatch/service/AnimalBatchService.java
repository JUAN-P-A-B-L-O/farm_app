package com.jpsoftware.farmapp.animalbatch.service;

import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.animalbatch.dto.AnimalBatchResponse;
import com.jpsoftware.farmapp.animalbatch.dto.CreateAnimalBatchRequest;
import com.jpsoftware.farmapp.animalbatch.dto.UpdateAnimalBatchRequest;
import com.jpsoftware.farmapp.animalbatch.entity.AnimalBatchEntity;
import com.jpsoftware.farmapp.animalbatch.entity.AnimalBatchMemberEntity;
import com.jpsoftware.farmapp.animalbatch.mapper.AnimalBatchMapper;
import com.jpsoftware.farmapp.animalbatch.repository.AnimalBatchMemberRepository;
import com.jpsoftware.farmapp.animalbatch.repository.AnimalBatchRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AnimalBatchService {

    private final AnimalBatchRepository animalBatchRepository;
    private final AnimalBatchMemberRepository animalBatchMemberRepository;
    private final AnimalRepository animalRepository;
    private final AnimalBatchMapper animalBatchMapper;
    private final FarmAccessService farmAccessService;

    public AnimalBatchService(
            AnimalBatchRepository animalBatchRepository,
            AnimalBatchMemberRepository animalBatchMemberRepository,
            AnimalRepository animalRepository,
            AnimalBatchMapper animalBatchMapper,
            FarmAccessService farmAccessService) {
        this.animalBatchRepository = animalBatchRepository;
        this.animalBatchMemberRepository = animalBatchMemberRepository;
        this.animalRepository = animalRepository;
        this.animalBatchMapper = animalBatchMapper;
        this.farmAccessService = farmAccessService;
    }

    @Transactional
    public AnimalBatchResponse create(CreateAnimalBatchRequest request, String farmId) {
        validateRequest(request);
        String resolvedFarmId = validateRequiredFarmId(farmId);
        List<AnimalEntity> animals = validateBatchAnimals(request.getAnimalIds(), resolvedFarmId);

        AnimalBatchEntity batchEntity = animalBatchMapper.toEntity(request);
        batchEntity.setName(request.getName().trim());
        batchEntity.setFarmId(resolvedFarmId);
        AnimalBatchEntity savedBatch = animalBatchRepository.save(batchEntity);
        saveMembers(savedBatch.getId(), animals);
        return animalBatchMapper.toResponse(savedBatch, toAnimalSummaries(animals));
    }

    @Transactional(readOnly = true)
    public List<AnimalBatchResponse> findAll(String search, String farmId) {
        String resolvedFarmId = validateRequiredFarmId(farmId);
        return toResponses(animalBatchRepository.findAll(buildSpecification(search, resolvedFarmId)));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<AnimalBatchResponse> findAllPaginated(String search, String farmId, int page, int size) {
        String resolvedFarmId = validateRequiredFarmId(farmId);
        Page<AnimalBatchEntity> batches = animalBatchRepository.findAll(
                buildSpecification(search, resolvedFarmId),
                PageRequest.of(page, size));
        List<AnimalBatchResponse> responses = toResponses(batches.getContent());
        return new PaginatedResponse<>(
                responses,
                batches.getNumber(),
                batches.getSize(),
                batches.getTotalElements(),
                batches.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AnimalBatchResponse findById(String id, String farmId) {
        return toResponse(findBatchEntity(id, validateRequiredFarmId(farmId)));
    }

    @Transactional
    public AnimalBatchResponse update(String id, UpdateAnimalBatchRequest request, String farmId) {
        validateUpdateRequest(request);
        String resolvedFarmId = validateRequiredFarmId(farmId);
        AnimalBatchEntity batchEntity = findBatchEntity(id, resolvedFarmId);
        List<AnimalEntity> animals = validateBatchAnimals(request.getAnimalIds(), resolvedFarmId);

        batchEntity.setName(request.getName().trim());
        AnimalBatchEntity savedBatch = animalBatchRepository.save(batchEntity);
        animalBatchMemberRepository.deleteByBatchId(savedBatch.getId());
        saveMembers(savedBatch.getId(), animals);
        return animalBatchMapper.toResponse(savedBatch, toAnimalSummaries(animals));
    }

    @Transactional
    public void delete(String id, String farmId) {
        AnimalBatchEntity batchEntity = findBatchEntity(id, validateRequiredFarmId(farmId));
        animalBatchMemberRepository.deleteByBatchId(batchEntity.getId());
        animalBatchRepository.delete(batchEntity);
    }

    @Transactional(readOnly = true)
    public List<AnimalEntity> getActiveAnimals(String batchId, String farmId) {
        AnimalBatchEntity batchEntity = findBatchEntity(batchId, validateRequiredFarmId(farmId));
        List<AnimalEntity> animals = findAnimalsByBatch(batchEntity);
        if (animals.isEmpty()) {
            throw new ValidationException("Batch must contain at least one animal");
        }
        for (AnimalEntity animal : animals) {
            if (!AnimalEntity.STATUS_ACTIVE.equals(animal.getStatus())) {
                throw new ValidationException("Animal must be ACTIVE for batch operations");
            }
        }
        return animals;
    }

    private AnimalBatchEntity findBatchEntity(String id, String farmId) {
        if (!StringUtils.hasText(id)) {
            throw new ValidationException("id must not be blank");
        }
        return animalBatchRepository.findByIdAndFarmId(id, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
    }

    private void validateRequest(CreateAnimalBatchRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (request.getAnimalIds() == null || request.getAnimalIds().isEmpty()) {
            throw new ValidationException("animalIds must not be empty");
        }
    }

    private void validateUpdateRequest(UpdateAnimalBatchRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (request.getAnimalIds() == null || request.getAnimalIds().isEmpty()) {
            throw new ValidationException("animalIds must not be empty");
        }
    }

    private String validateRequiredFarmId(String farmId) {
        return farmAccessService.validateAccessibleFarm(farmId);
    }

    private List<AnimalEntity> validateBatchAnimals(Collection<String> animalIds, String farmId) {
        Set<String> normalizedAnimalIds = animalIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalizedAnimalIds.isEmpty()) {
            throw new ValidationException("animalIds must not be empty");
        }

        Map<String, AnimalEntity> animalsById = animalRepository.findAllById(normalizedAnimalIds).stream()
                .collect(Collectors.toMap(AnimalEntity::getId, animal -> animal, (left, right) -> left, LinkedHashMap::new));
        if (animalsById.size() != normalizedAnimalIds.size()) {
            throw new ResourceNotFoundException("Animal not found");
        }

        List<AnimalEntity> animals = new ArrayList<>(normalizedAnimalIds.size());
        for (String animalId : normalizedAnimalIds) {
            AnimalEntity animal = animalsById.get(animalId);
            if (animal == null) {
                throw new ResourceNotFoundException("Animal not found");
            }
            if (!farmId.equals(animal.getFarmId())) {
                throw new ValidationException("All animals in the batch must belong to the same farm");
            }
            animals.add(animal);
        }
        return animals;
    }

    private void saveMembers(String batchId, List<AnimalEntity> animals) {
        animalBatchMemberRepository.saveAll(animals.stream()
                .map(animal -> new AnimalBatchMemberEntity(null, batchId, animal.getId()))
                .toList());
    }

    private List<AnimalBatchResponse> toResponses(List<AnimalBatchEntity> batches) {
        if (batches.isEmpty()) {
            return List.of();
        }

        List<String> batchIds = batches.stream().map(AnimalBatchEntity::getId).toList();
        Map<String, List<AnimalBatchMemberEntity>> membersByBatchId = animalBatchMemberRepository.findByBatchIdIn(batchIds).stream()
                .collect(Collectors.groupingBy(AnimalBatchMemberEntity::getBatchId, LinkedHashMap::new, Collectors.toList()));

        Set<String> animalIds = membersByBatchId.values().stream()
                .flatMap(List::stream)
                .map(AnimalBatchMemberEntity::getAnimalId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, AnimalEntity> animalsById = animalRepository.findAllById(animalIds).stream()
                .collect(Collectors.toMap(AnimalEntity::getId, animal -> animal));

        return batches.stream()
                .map(batch -> animalBatchMapper.toResponse(batch, toAnimalSummaries(membersByBatchId.get(batch.getId()), animalsById)))
                .toList();
    }

    private AnimalBatchResponse toResponse(AnimalBatchEntity batchEntity) {
        return animalBatchMapper.toResponse(batchEntity, toAnimalSummaries(findAnimalsByBatch(batchEntity)));
    }

    private List<AnimalEntity> findAnimalsByBatch(AnimalBatchEntity batchEntity) {
        List<AnimalBatchMemberEntity> members = animalBatchMemberRepository.findByBatchId(batchEntity.getId());
        if (members.isEmpty()) {
            return List.of();
        }

        Map<String, AnimalEntity> animalsById = animalRepository.findAllById(
                        members.stream().map(AnimalBatchMemberEntity::getAnimalId).toList())
                .stream()
                .collect(Collectors.toMap(AnimalEntity::getId, animal -> animal));

        List<AnimalEntity> animals = new ArrayList<>(members.size());
        for (AnimalBatchMemberEntity member : members) {
            AnimalEntity animal = animalsById.get(member.getAnimalId());
            if (animal == null) {
                throw new ResourceNotFoundException("Animal not found");
            }
            if (!batchEntity.getFarmId().equals(animal.getFarmId())) {
                throw new ValidationException("All animals in the batch must belong to the same farm");
            }
            animals.add(animal);
        }
        return animals;
    }

    private List<AnimalSummaryResponse> toAnimalSummaries(List<AnimalEntity> animals) {
        return animals.stream()
                .map(animal -> new AnimalSummaryResponse(animal.getId(), animal.getTag()))
                .toList();
    }

    private List<AnimalSummaryResponse> toAnimalSummaries(
            List<AnimalBatchMemberEntity> members,
            Map<String, AnimalEntity> animalsById) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        List<AnimalSummaryResponse> animals = new ArrayList<>(members.size());
        for (AnimalBatchMemberEntity member : members) {
            AnimalEntity animal = animalsById.get(member.getAnimalId());
            if (animal != null) {
                animals.add(new AnimalSummaryResponse(animal.getId(), animal.getTag()));
            }
        }
        return animals;
    }

    private Specification<AnimalBatchEntity> buildSpecification(String search, String farmId) {
        String normalizedSearch = normalizeFilter(search);
        Specification<AnimalBatchEntity> specification = Specification.where((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("farmId"), farmId));

        if (normalizedSearch != null) {
            String searchPattern = "%" + normalizedSearch + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern));
        }
        return specification;
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
