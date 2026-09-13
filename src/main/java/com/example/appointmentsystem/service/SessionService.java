package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Server-side session store. The token is only an opaque lookup key. */
@Service
public class SessionService {
    public static final Duration SESSION_DURATION = Duration.ofHours(1);

    private final UserRepository users;
    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    public SessionService(UserRepository users) {
        this.users = users;
    }

    public String start(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(user.getId(), Instant.now().plus(SESSION_DURATION)));
        return token;
    }

    public void end(String token) {
        sessions.remove(token);
    }

    public User requireUser(String token) {
        Session session = sessions.get(token);
        if (session == null || !session.expiresAt().isAfter(Instant.now())) {
            sessions.remove(token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesi kedaluwarsa");
        }
        return users.findById(session.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User tidak ditemukan"));
    }

    private record Session(UUID userId, Instant expiresAt) { }
}
