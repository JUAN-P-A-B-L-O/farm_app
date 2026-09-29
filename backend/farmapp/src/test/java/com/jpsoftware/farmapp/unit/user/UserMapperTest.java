package com.jpsoftware.farmapp.unit.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void shouldMapExplicitUserPlan() {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        entity.setName("Jane Doe");
        entity.setEmail("jane@farm.com");
        entity.setRole("MANAGER");
        entity.setActive(true);
        entity.setAvatarUrl("https://example.com/avatar.png");
        entity.setPlan(UserPlan.PRO);

        UserResponse response = userMapper.toResponse(entity, List.of("farm-1"));

        assertEquals("PRO", response.getPlan());
    }

    @Test
    void shouldFallbackToDefaultPlanWhenLegacyEntityHasNullPlan() {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        entity.setName("Legacy User");
        entity.setEmail("legacy@farm.com");
        entity.setRole("WORKER");
        entity.setActive(true);
        ReflectionTestUtils.setField(entity, "plan", null);

        UserResponse response = userMapper.toResponse(entity, List.of("farm-1"));

        assertEquals("FREE", response.getPlan());
    }
}
