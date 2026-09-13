package com.example.appointmentsystem.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AppointmentRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String start,
        @NotBlank @Size(max = 255) String end,
        List<@NotBlank @Size(max = 255) String> invitees) { }
