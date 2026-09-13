package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    public User authenticate(String username) {
        return users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username tidak ditemukan"));
    }

    public List<User> findAll() {
        return users.findAll();
    }

    public User create(String name, String username, String timezone) {
        if (users.findByUsernameIgnoreCase(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username sudah digunakan");
        }
        try {
            if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
                throw new DateTimeException("Gunakan nama zona waktu, contoh Asia/Jakarta");
            }
            return users.save(new User(name, username, timezone));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username sudah digunakan");
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timezone tidak valid");
        }
    }
}
