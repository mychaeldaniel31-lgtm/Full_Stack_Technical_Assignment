package com.example.appointmentsystem.web;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.web.dto.AppointmentResponse;
import com.example.appointmentsystem.web.dto.UserResponse;
import org.springframework.stereotype.Component;

/** Maps persistence objects to stable API representations. */
@Component
public class ApiMapper {
    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getPreferredTimezone());
    }

    public AppointmentResponse toAppointmentResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getTitle(),
                appointment.getCreator().getUsername(),
                appointment.getStart().toString(),
                appointment.getEnd().toString(),
                appointment.getInvitees().stream().map(User::getUsername).sorted().toList());
    }
}
