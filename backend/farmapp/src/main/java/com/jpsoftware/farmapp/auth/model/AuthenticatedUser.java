package com.jpsoftware.farmapp.auth.model;

import com.jpsoftware.farmapp.user.entity.UserPlan;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID id, List<String> roles, UserPlan plan) {
}
