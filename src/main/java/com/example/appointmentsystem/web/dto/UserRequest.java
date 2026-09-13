package com.example.appointmentsystem.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(max = 255) String preferredTimezone) { }
