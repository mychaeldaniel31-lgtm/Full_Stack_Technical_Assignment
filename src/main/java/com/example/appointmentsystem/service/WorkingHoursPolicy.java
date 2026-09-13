package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.User;

import java.time.Instant;
import java.util.Collection;

/** Policy abstraction keeps calendar rules independent from appointment persistence. */
public interface WorkingHoursPolicy {
    void validate(Instant start, Instant end, Collection<User> participants);
}
