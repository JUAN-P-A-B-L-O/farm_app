package com.jpsoftware.farmapp.user.mapper;

import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(CreateUserRequest request) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(request.getName());
        userEntity.setEmail(request.getEmail());
        userEntity.setRole(request.getRole());
        return userEntity;
    }

    public UserResponse toResponse(UserEntity entity) {
        return toResponse(entity, List.of());
    }

    public UserResponse toResponse(UserEntity entity, List<String> farmIds) {
        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole(),
                entity.isActive(),
                farmIds);
    }
}
