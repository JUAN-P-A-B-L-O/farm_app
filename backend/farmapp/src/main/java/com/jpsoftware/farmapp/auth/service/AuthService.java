package com.jpsoftware.farmapp.auth.service;

import com.jpsoftware.farmapp.auth.dto.LoginResponse;
import com.jpsoftware.farmapp.auth.dto.RegisterRequest;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.InvalidCredentialsException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserEntity user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive() || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = tokenService.generateToken(user);
        UserResponse userResponse = userMapper.toResponse(user);
        return new LoginResponse(accessToken, userResponse);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }

        String normalizedEmail = normalizeEmail(request.getEmail());
        userRepository.findByEmail(normalizedEmail)
                .ifPresent(existingUser -> {
                    throw new ConflictException("User with this email already exists");
                });

        UserEntity user = new UserEntity();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setRole("MANAGER");
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser, List.of());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
