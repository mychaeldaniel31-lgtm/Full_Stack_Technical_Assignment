package com.example.appointmentsystem;

import com.example.appointmentsystem.service.SessionService;
import com.example.appointmentsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:api-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ApiFunctionalTests {
    @Autowired WebApplicationContext context;
    @Autowired SessionService sessions;
    @Autowired UserService users;
    MockMvc mvc;
    @BeforeEach void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).build(); }

    @Test void loginAndProtectedEndpoints() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"username\":\"siti\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").value(3600));
        mvc.perform(get("/api/appointments")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"username\":\"missing\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test void rejectsOversizedFieldsAndNullInvitees() throws Exception {
        mvc.perform(post("/api/users").contentType("application/json").content(
                "{\"name\":\"" + "a".repeat(256) + "\",\"username\":\"long-name\",\"preferredTimezone\":\"UTC\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/appointments").header("Authorization", "Bearer " + sessions.start(users.authenticate("siti")))
                .contentType("application/json").content("""
                {"title":"Invalid","start":"2040-01-01T01:00:00Z","end":"2040-01-01T02:00:00Z","invitees":[null]}
                """)).andExpect(status().isBadRequest());
    }

    @Test void rejectsJavaOnlyTimezones() throws Exception {
        for (String timezone : new String[]{"UTC+07:00", "GMT+07:00"}) {
            mvc.perform(post("/api/users").contentType("application/json").content(
                    "{\"name\":\"Offset user\",\"username\":\"offset-" + timezone + "\",\"preferredTimezone\":\"" + timezone + "\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test void sessionExpiresAtExactlyOneHour() {
        var user = users.authenticate("siti");
        Instant now = Instant.parse("2040-01-01T00:00:00Z");
        try (var clock = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            clock.when(Instant::now).thenReturn(now);
            String token = sessions.start(user);
            clock.when(Instant::now).thenReturn(now.plusSeconds(3599));
            assertThat(sessions.requireUser(token).getId()).isEqualTo(user.getId());
            clock.when(Instant::now).thenReturn(now.plusSeconds(3600));
            assertThatThrownBy(() -> sessions.requireUser(token)).isInstanceOf(ResponseStatusException.class);
        }
    }

    @Test void logoutRevokesTokenOnServer() throws Exception {
        String token = sessions.start(users.authenticate("siti"));
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test void inviteeCanSeeAppointmentAndValidationIncludesReason() throws Exception {
        String siti = sessions.start(users.authenticate("siti"));
        String maya = sessions.start(users.authenticate("maya"));
        mvc.perform(post("/api/appointments").header("Authorization", "Bearer " + siti)
                .contentType("application/json").content("""
                {"title":"API meeting","start":"2040-01-01T01:00:00Z","end":"2040-01-01T02:00:00Z","invitees":["maya"]}
                """)).andExpect(status().isCreated());
        mvc.perform(get("/api/appointments").header("Authorization", "Bearer " + maya))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.hasItem("API meeting")));
        mvc.perform(post("/api/appointments").header("Authorization", "Bearer " + siti)
                .contentType("application/json").content("""
                {"title":"Invalid","start":"2040-01-01T04:00:00Z","end":"2040-01-01T05:00:00Z","invitees":["maya"]}
                """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("maya")));
    }
}
