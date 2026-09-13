package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repo.AppointmentRepository;
import com.example.appointmentsystem.repo.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.*;

@Service
public class AppointmentService {
    private final AppointmentRepository appointments;
    private final UserRepository users;
    private final WorkingHoursPolicy workingHours;

    public AppointmentService(AppointmentRepository appointments, UserRepository users, WorkingHoursPolicy workingHours) {
        this.appointments = appointments;
        this.users = users;
        this.workingHours = workingHours;
    }

    public List<Appointment> findUpcoming(User user, int page, int size) {
        List<UUID> ids = appointments.upcomingIds(user.getId(), Instant.now(), PageRequest.of(page, size));
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, Appointment> details = new HashMap<>();
        appointments.findWithParticipants(ids).forEach(appointment -> details.put(appointment.getId(), appointment));
        return ids.stream().map(details::get).filter(Objects::nonNull).toList();
    }

    public Appointment create(User creator, String title, String startText, String endText, List<String> usernames) {
        try {
            Instant start = Instant.parse(startText);
            Instant end = Instant.parse(endText);
            Set<User> invitees = resolveInvitees(usernames);
            Set<User> participants = new HashSet<>(invitees);
            participants.add(creator);
            workingHours.validate(start, end, participants);
            return appointments.save(new Appointment(title, creator, start, end, invitees));
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Gunakan datetime ISO-8601 UTC, contoh 2026-09-13T09:00:00Z");
        }
    }

    private Set<User> resolveInvitees(List<String> usernames) {
        Set<User> invitees = new HashSet<>();
        for (String username : usernames == null ? List.<String>of() : usernames) {
            invitees.add(users.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invitee tidak ditemukan: " + username)));
        }
        return invitees;
    }
}
