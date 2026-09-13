package com.example.appointmentsystem.web.dto;

import java.util.UUID;

public record UserResponse(UUID id, String name, String username, String preferredTimezone) { }
