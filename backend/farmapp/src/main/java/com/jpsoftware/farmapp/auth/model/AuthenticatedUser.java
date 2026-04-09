package com.jpsoftware.farmapp.auth.model;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID id, List<String> roles) {
}
