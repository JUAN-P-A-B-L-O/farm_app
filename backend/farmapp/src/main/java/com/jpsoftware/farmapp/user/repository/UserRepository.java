package com.jpsoftware.farmapp.user.repository;

import com.jpsoftware.farmapp.user.entity.UserEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
