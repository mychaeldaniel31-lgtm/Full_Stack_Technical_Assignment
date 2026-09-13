package com.example.appointmentsystem;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repo.AppointmentRepository;
import com.example.appointmentsystem.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.datasource.url=jdbc:h2:mem:query-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AppointmentQueryTests {
    @Autowired AppointmentRepository appointments;
    @Autowired UserRepository users;

    @Test
    void storageRejectsCaseVariantUsernamesEvenWithoutServicePrecheck() {
        users.saveAndFlush(new User("First", "MixedCase", "UTC"));
        assertThatThrownBy(() -> users.saveAndFlush(new User("Second", "mixedcase", "UTC")))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void pagesVisibleAppointmentsWithoutDuplicatesAndLoadsAllInvitees() {
        User creator = users.save(new User("Creator", "query-creator", "UTC"));
        User invitee = users.save(new User("Invitee", "query-invitee", "UTC"));
        User other = users.save(new User("Other", "query-other", "UTC"));
        Instant now = Instant.parse("2040-01-01T00:00:00Z");
        Appointment first = appointments.save(new Appointment("First", creator,
                now.plusSeconds(3600), now.plusSeconds(7200), Set.of(invitee, other)));
        Appointment second = appointments.save(new Appointment("Second", creator,
                now.plusSeconds(10800), now.plusSeconds(14400), Set.of(invitee)));
        appointments.save(new Appointment("Unrelated", other,
                now.plusSeconds(18000), now.plusSeconds(21600), Set.of()));
        appointments.flush();

        assertThat(appointments.upcomingIds(creator.getId(), now, PageRequest.of(0, 1)))
                .containsExactly(first.getId());
        assertThat(appointments.upcomingIds(creator.getId(), now, PageRequest.of(1, 1)))
                .containsExactly(second.getId());
        var invitedIds = appointments.upcomingIds(invitee.getId(), now, PageRequest.of(0, 10));
        assertThat(invitedIds).containsExactly(first.getId(), second.getId());
        assertThat(appointments.findWithParticipants(invitedIds).get(0).getInvitees())
                .extracting(User::getUsername).containsExactlyInAnyOrder("query-invitee", "query-other");
        assertThat(appointments.upcomingIds(invitee.getId(), now, PageRequest.of(2, 1))).isEmpty();
    }
}
