package com.example.appointmentsystem.web;

import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.service.AppointmentService;
import com.example.appointmentsystem.service.SessionService;
import com.example.appointmentsystem.service.UserService;
import com.example.appointmentsystem.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserService userService;
    private final SessionService sessionService;
    private final AppointmentService appointmentService;
    private final ApiMapper mapper;

    public ApiController(UserService userService, SessionService sessionService,
                         AppointmentService appointmentService, ApiMapper mapper) {
        this.userService = userService; this.sessionService = sessionService;
        this.appointmentService = appointmentService; this.mapper = mapper;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.username());
        return Map.of("token", sessionService.start(user), "user", mapper.toUserResponse(user), "expiresInSeconds", 3600);
    }

    @PostMapping("/auth/logout")
    public Map<String, Object> logout(@RequestHeader("Authorization") String authorization) {
        sessionService.end(authorization.substring("Bearer ".length()));
        return Map.of();
    }

    @GetMapping("/me") public UserResponse me(@RequestAttribute User user) { return mapper.toUserResponse(user); }

    @GetMapping("/users")
    public List<UserResponse> users() { return userService.findAll().stream().map(mapper::toUserResponse).toList(); }

    @PostMapping("/users")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return mapper.toUserResponse(userService.create(request.name(), request.username(), request.preferredTimezone()));
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> appointments(@RequestAttribute User user,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "50") int size) {
        int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 100);
        return appointmentService.findUpcoming(user, safePage, safeSize).stream().map(mapper::toAppointmentResponse).toList();
    }

    @PostMapping("/appointments")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public AppointmentResponse createAppointment(@RequestAttribute User user, @Valid @RequestBody AppointmentRequest request) {
        return mapper.toAppointmentResponse(appointmentService.create(user, request.title(), request.start(), request.end(), request.invitees()));
    }
}
