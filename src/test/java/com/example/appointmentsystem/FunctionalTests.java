package com.example.appointmentsystem;

import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.service.BusinessHoursPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class FunctionalTests {
    private final BusinessHoursPolicy policy = new BusinessHoursPolicy();

    @Test void rejectsOvernightAndMultiDayAppointments() {
        var users = List.of(new User("Test", "test", "UTC"));
        for (String end : List.of("2040-01-02T07:00:00Z", "2040-01-02T10:00:00Z")) {
            assertThatThrownBy(() -> policy.validate(Instant.parse("2040-01-01T16:00:00Z"),
                    Instant.parse(end), users)).isInstanceOf(ResponseStatusException.class);
        }
    }

    @Test void validatesEveryParticipantAndDstOffsets() {
        var jakarta = new User("Siti", "siti", "Asia/Jakarta");
        var auckland = new User("Maya", "maya", "Pacific/Auckland");
        assertThatCode(() -> policy.validate(Instant.parse("2040-01-01T01:00:00Z"),
                Instant.parse("2040-01-01T04:00:00Z"), List.of(jakarta, auckland))).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validate(Instant.parse("2040-01-01T04:00:00Z"),
                Instant.parse("2040-01-01T05:00:00Z"), List.of(jakarta, auckland)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("maya");
        assertThatCode(() -> policy.validate(Instant.parse("2040-07-01T04:00:00Z"),
                Instant.parse("2040-07-01T05:00:00Z"), List.of(jakarta, auckland))).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validate(Instant.parse("2040-01-01T01:00:00Z"),
                Instant.parse("2040-01-01T02:00:00Z"), List.of(jakarta,
                        new User("Alex", "alex", "America/New_York"))))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("alex");
    }
}
