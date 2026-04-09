package com.jpsoftware.farmapp.auth.service;

import com.jpsoftware.farmapp.user.entity.UserEntity;
import java.util.UUID;

public interface TokenService {

    String generateToken(UserEntity user);

    boolean validateToken(String token);

    UUID extractUserId(String token);
}
