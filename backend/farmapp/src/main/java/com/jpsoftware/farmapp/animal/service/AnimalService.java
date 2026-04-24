package com.jpsoftware.farmapp.animal.service;

import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.SellAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.mapper.AnimalMapper;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.currency.CurrencyConversionUtils;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.shared.util.CsvColumn;
import com.jpsoftware.farmapp.shared.util.CsvExportUtils;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AnimalService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            AnimalEntity.STATUS_ACTIVE,
            AnimalEntity.STATUS_INACTIVE,
            AnimalEntity.STATUS_SOLD,
            AnimalEntity.STATUS_DEAD);
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            AnimalEntity.ORIGIN_BORN,
            AnimalEntity.ORIGIN_PURCHASED);

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;
    private final FarmAccessService farmAccessService;

    public AnimalService(AnimalRepository animalRepository, AnimalMapper animalMapper, FarmAccessService farmAccessService) {
        this.animalRepository = animalRepository;
        this.animalMapper = animalMapper;
        this.farmAccessService = farmAccessService;
    }

    @Transactional
    public AnimalResponse create(CreateAnimalRequest request) {
        validateInput(request);
        farmAccessService.validateAccessibleFarm(request.getFarmId());
        ensureTagIsUnique(request.getTag());
        String normalizedOrigin = normalizeOrigin(request.getOrigin());
        Double normalizedAcquisitionCost = normalizeAcquisitionCost(normalizedOrigin, request.getAcquisitionCost());

        AnimalEntity animalEntity = animalMapper.toEntity(request);
        animalEntity.setStatus(AnimalEntity.STATUS_ACTIVE);
        animalEntity.setOrigin(normalizedOrigin);
        animalEntity.setAcquisitionCost(normalizedAcquisitionCost);
        AnimalEntity savedAnimal = animalRepository.save(animalEntity);

        return animalMapper.toResponse(savedAnimal);
    }

    @Transactional(readOnly = true)
    public AnimalResponse findById(String id, String farmId) {
        AnimalEntity animalEntity = findAnimal(id, farmId);

        return animalMapper.toResponse(animalEntity);
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> findAll(String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        List<AnimalEntity> animals = StringUtils.hasText(farmId)
                ? animalRepository.findByFarmId(farmId)
                : animalRepository.findAll();

        return animals.stream()
                .map(animalMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<AnimalResponse> findAllPaginated(String farmId, int page, int size) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        Page<AnimalEntity> animals = StringUtils.hasText(farmId)
                ? animalRepository.findByFarmId(farmId, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "tag")))
                : animalRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "tag")));
        Page<AnimalResponse> responses = animals.map(animalMapper::toResponse);
        return new PaginatedResponse<>(
                responses.getContent(),
                responses.getNumber(),
                responses.getSize(),
                responses.getTotalElements(),
                responses.getTotalPages());
    }

    @Transactional(readOnly = true)
    public String exportAll(String farmId) {
        return exportAll(farmId, null);
    }

    @Transactional(readOnly = true)
    public String exportAll(String farmId, String currency) {
        return CsvExportUtils.write(findAll(farmId).stream()
                .map(animal -> AnimalResponse.builder()
                        .id(animal.getId())
                        .tag(animal.getTag())
                        .breed(animal.getBreed())
                        .birthDate(animal.getBirthDate())
                        .origin(animal.getOrigin())
                        .status(animal.getStatus())
                        .acquisitionCost(CurrencyConversionUtils.convertMonetaryValue(animal.getAcquisitionCost(), currency))
                        .salePrice(CurrencyConversionUtils.convertMonetaryValue(animal.getSalePrice(), currency))
                        .saleDate(animal.getSaleDate())
                        .farmId(animal.getFarmId())
                        .build())
                .toList(), List.of(
                new CsvColumn<>("id", AnimalResponse::getId),
                new CsvColumn<>("tag", AnimalResponse::getTag),
                new CsvColumn<>("breed", AnimalResponse::getBreed),
                new CsvColumn<>("birthDate", AnimalResponse::getBirthDate),
                new CsvColumn<>("origin", AnimalResponse::getOrigin),
                new CsvColumn<>("status", AnimalResponse::getStatus),
                new CsvColumn<>("acquisitionCost", AnimalResponse::getAcquisitionCost),
                new CsvColumn<>("salePrice", AnimalResponse::getSalePrice),
                new CsvColumn<>("saleDate", AnimalResponse::getSaleDate),
                new CsvColumn<>("farmId", AnimalResponse::getFarmId)));
    }

    @Transactional
    public AnimalResponse update(String id, UpdateAnimalRequest request, String farmId) {
        if (request == null) {
            throw new IllegalArgumentException("Update animal request must not be null");
        }

        AnimalEntity animalEntity = findAnimal(id, farmId);

        validateTagUpdate(animalEntity, request);
        if (request.getFarmId() != null) {
            farmAccessService.validateAccessibleFarm(request.getFarmId());
        }
        applyUpdates(animalEntity, request);

        AnimalEntity updatedAnimal = animalRepository.save(animalEntity);
        return animalMapper.toResponse(updatedAnimal);
    }

    @Transactional
    public AnimalResponse sell(String id, SellAnimalRequest request, String farmId) {
        if (request == null) {
            throw new IllegalArgumentException("Sell animal request must not be null");
        }

        AnimalEntity animalEntity = findAnimal(id, farmId);
        validateSellable(animalEntity);

        Double salePrice = normalizeSalePrice(request.getSalePrice());
        LocalDate saleDate = request.getSaleDate() != null ? request.getSaleDate() : LocalDate.now();

        animalEntity.setStatus(AnimalEntity.STATUS_SOLD);
        animalEntity.setSalePrice(salePrice);
        animalEntity.setSaleDate(saleDate);

        AnimalEntity soldAnimal = animalRepository.save(animalEntity);
        return animalMapper.toResponse(soldAnimal);
    }

    @Transactional
    public void delete(String id, String farmId) {
        AnimalEntity animalEntity = findAnimal(id, farmId);

        if (AnimalEntity.STATUS_INACTIVE.equals(animalEntity.getStatus())) {
            return;
        }

        animalEntity.setStatus(AnimalEntity.STATUS_INACTIVE);
        animalRepository.save(animalEntity);
    }

    private void validateInput(CreateAnimalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create animal request must not be null");
        }
        if (!StringUtils.hasText(request.getTag())) {
            throw new IllegalArgumentException("Animal tag must not be blank");
        }
        if (!StringUtils.hasText(request.getBreed())) {
            throw new IllegalArgumentException("Animal breed must not be blank");
        }
        if (request.getBirthDate() == null) {
            throw new IllegalArgumentException("Animal birthDate must not be null");
        }
        if (!StringUtils.hasText(request.getOrigin())) {
            throw new IllegalArgumentException("Animal origin must not be blank");
        }
        if (!StringUtils.hasText(request.getFarmId())) {
            throw new IllegalArgumentException("Animal farmId must not be blank");
        }
    }

    private void applyUpdates(AnimalEntity animalEntity, UpdateAnimalRequest request) {
        if (request.getTag() != null) {
            animalEntity.setTag(request.getTag());
        }
        if (request.getBreed() != null) {
            animalEntity.setBreed(request.getBreed());
        }
        if (request.getBirthDate() != null) {
            animalEntity.setBirthDate(request.getBirthDate());
        }
        if (request.getStatus() != null) {
            animalEntity.setStatus(normalizeStatusForUpdate(animalEntity.getStatus(), request.getStatus()));
        }
        if (request.getOrigin() != null) {
            animalEntity.setOrigin(normalizeOrigin(request.getOrigin()));
        }
        if (request.getAcquisitionCost() != null || request.getOrigin() != null) {
            animalEntity.setAcquisitionCost(normalizeAcquisitionCost(
                    animalEntity.getOrigin(),
                    request.getAcquisitionCost() != null ? request.getAcquisitionCost() : animalEntity.getAcquisitionCost()));
        }
        if (request.getFarmId() != null) {
            animalEntity.setFarmId(request.getFarmId());
        }
    }

    private void ensureTagIsUnique(String tag) {
        if (animalRepository.existsByTag(tag)) {
            throw new DataIntegrityViolationException("Animal with this tag already exists");
        }
    }

    private void validateTagUpdate(AnimalEntity animalEntity, UpdateAnimalRequest request) {
        if (request.getTag() == null) {
            return;
        }

        if (!request.getTag().equals(animalEntity.getTag())) {
            ensureTagIsUnique(request.getTag());
        }
    }

    private AnimalEntity findAnimal(String id, String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        return (StringUtils.hasText(farmId)
                ? animalRepository.findByIdAndFarmId(id, farmId)
                : animalRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found"));
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new ValidationException("Animal status must be ACTIVE, SOLD, DEAD, or INACTIVE");
        }
        return normalizedStatus;
    }

    private String normalizeStatusForUpdate(String currentStatus, String requestedStatus) {
        String normalizedStatus = normalizeStatus(requestedStatus);

        if (AnimalEntity.STATUS_SOLD.equals(normalizedStatus) && !AnimalEntity.STATUS_SOLD.equals(currentStatus)) {
            throw new ValidationException("Use the sell action to mark an animal as SOLD");
        }

        if (AnimalEntity.STATUS_SOLD.equals(currentStatus) && !AnimalEntity.STATUS_SOLD.equals(normalizedStatus)) {
            throw new ValidationException("Sold animals cannot transition to another status");
        }

        return normalizedStatus;
    }

    private String normalizeOrigin(String origin) {
        String normalizedOrigin = origin.trim().toUpperCase();
        if (!ALLOWED_ORIGINS.contains(normalizedOrigin)) {
            throw new ValidationException("Animal origin must be PURCHASED or BORN");
        }
        return normalizedOrigin;
    }

    private Double normalizeAcquisitionCost(String origin, Double acquisitionCost) {
        if (AnimalEntity.ORIGIN_PURCHASED.equals(origin)) {
            if (acquisitionCost == null || acquisitionCost <= 0) {
                throw new ValidationException("Animal acquisitionCost must be greater than zero for purchased animals");
            }
            DecimalScaleUtils.requireMaxScale(acquisitionCost, "acquisitionCost");
            return DecimalScaleUtils.normalize(acquisitionCost);
        }

        if (acquisitionCost != null && acquisitionCost > 0) {
            DecimalScaleUtils.requireMaxScale(acquisitionCost, "acquisitionCost");
        }
        return null;
    }

    private void validateSellable(AnimalEntity animalEntity) {
        if (AnimalEntity.STATUS_SOLD.equals(animalEntity.getStatus())) {
            throw new ValidationException("Animal is already sold");
        }
        if (AnimalEntity.STATUS_DEAD.equals(animalEntity.getStatus())
                || AnimalEntity.STATUS_INACTIVE.equals(animalEntity.getStatus())) {
            throw new ValidationException("Only active animals can be sold");
        }
    }

    private Double normalizeSalePrice(Double salePrice) {
        if (salePrice == null || salePrice <= 0) {
            throw new ValidationException("Animal salePrice must be greater than zero");
        }

        DecimalScaleUtils.requireMaxScale(salePrice, "salePrice");
        return DecimalScaleUtils.normalize(salePrice);
    }
}
