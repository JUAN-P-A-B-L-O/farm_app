package com.jpsoftware.farmapp.unit.shared.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.shared.config.DefaultAdminInitializer;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class DefaultAdminInitializerTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void shouldCreateDefaultAdminWhenNoUsersExist() throws Exception {
        when(userRepository.count()).thenReturn(0L);

        DefaultAdminInitializer initializer = new DefaultAdminInitializer(
                userRepository,
                passwordEncoder,
                "admin@farmapp.com",
                "admin123");

        initializer.run();

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertEquals("Default Admin", savedUser.getName());
        assertEquals("admin@farmapp.com", savedUser.getEmail());
        assertEquals("MANAGER", savedUser.getRole());
        assertNotEquals("admin123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("admin123", savedUser.getPassword()));
    }

    @Test
    void shouldDoNothingWhenUsersAlreadyExist() throws Exception {
        when(userRepository.count()).thenReturn(1L);

        DefaultAdminInitializer initializer = new DefaultAdminInitializer(
                userRepository,
                passwordEncoder,
                "admin@farmapp.com",
                "admin123");

        initializer.run();

        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
