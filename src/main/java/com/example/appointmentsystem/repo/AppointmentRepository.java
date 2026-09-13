package com.example.appointmentsystem.repo;
import com.example.appointmentsystem.model.Appointment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment,UUID>{
    // EXISTS checks membership without multiplying rows, so DISTINCT is unnecessary.
    @Query("select a.id from Appointment a " +
           "where a.start >= :now and (a.creator.id = :uid or exists " +
           "(select i.id from a.invitees i where i.id = :uid)) order by a.start, a.id")
    List<UUID> upcomingIds(UUID uid, Instant now, Pageable page);

    @EntityGraph(attributePaths = {"creator", "invitees"})
    @Query("select distinct a from Appointment a where a.id in :ids order by a.start")
    List<Appointment> findWithParticipants(List<UUID> ids);
}
