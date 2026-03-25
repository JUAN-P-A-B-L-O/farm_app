package com.jpsoftware.farmapp.user.service;

import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        validateInput(request);

        UserEntity userEntity = userMapper.toEntity(request);
        UserEntity savedUser = userRepository.save(userEntity);

        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(String id) {
        UserEntity userEntity = userRepository.findById(validateId(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(userEntity);
    }

    private void validateInput(CreateUserRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new ValidationException("email must not be blank");
        }
        if (!StringUtils.hasText(request.getRole())) {
            throw new ValidationException("role must not be blank");
        }
    }

    private UUID validateId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ValidationException("id must not be blank");
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("id must be a valid UUID");
        }
    }
}
