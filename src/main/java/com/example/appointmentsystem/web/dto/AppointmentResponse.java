package com.example.appointmentsystem.web.dto;

import java.util.List;
import java.util.UUID;

public record AppointmentResponse(
        UUID id, String title, String creator, String start, String end, List<String> invitees) { }
