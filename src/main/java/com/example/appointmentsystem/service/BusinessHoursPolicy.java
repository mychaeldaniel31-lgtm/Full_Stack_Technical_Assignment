package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.Collection;

@Component
public class BusinessHoursPolicy implements WorkingHoursPolicy {
    private static final LocalTime OPEN = LocalTime.of(8, 0);
    private static final LocalTime CLOSE = LocalTime.of(17, 0);

    @Override
    public void validate(Instant start, Instant end, Collection<User> participants) {
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Waktu selesai harus setelah waktu mulai");
        }
        for (User participant : participants) {
            ZoneId zone = ZoneId.of(participant.getPreferredTimezone());
            LocalTime localStart = start.atZone(zone).toLocalTime();
            LocalTime localEnd = end.atZone(zone).toLocalTime();
            if (!start.atZone(zone).toLocalDate().equals(end.atZone(zone).toLocalDate())
                    || localStart.isBefore(OPEN) || localEnd.isAfter(CLOSE)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Waktu di luar jam kerja 08:00-17:00 untuk " + participant.getUsername());
            }
        }
    }
}
