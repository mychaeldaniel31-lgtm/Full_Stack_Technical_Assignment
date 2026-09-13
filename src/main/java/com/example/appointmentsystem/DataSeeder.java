package com.example.appointmentsystem;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repo.AppointmentRepository;
import com.example.appointmentsystem.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Provides predictable demo accounts (spanning many timezones) and a few appointments for a fresh database. */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository users, AppointmentRepository appointments) {
        return args -> {
            // name -> [username, timezone]
            Map<String, String[]> demoUsers = new LinkedHashMap<>();
            demoUsers.put("Siti Admin",        new String[]{"siti",   "Asia/Jakarta"});
            demoUsers.put("Alex Product",      new String[]{"alex",   "America/New_York"});
            demoUsers.put("Maya Auckland",     new String[]{"maya",   "Pacific/Auckland"});
            demoUsers.put("Budi Bandung",      new String[]{"budi",   "Asia/Jakarta"});
            demoUsers.put("Wayan Denpasar",    new String[]{"wayan",  "Asia/Makassar"});
            demoUsers.put("Rizky Papua",       new String[]{"rizky",  "Asia/Jayapura"});
            demoUsers.put("Aiko Tokyo",        new String[]{"aiko",   "Asia/Tokyo"});
            demoUsers.put("Wei Beijing",       new String[]{"wei",    "Asia/Shanghai"});
            demoUsers.put("Minjun Seoul",      new String[]{"minjun", "Asia/Seoul"});
            demoUsers.put("Anjali Mumbai",     new String[]{"anjali", "Asia/Kolkata"});
            demoUsers.put("Farid Dubai",       new String[]{"farid",  "Asia/Dubai"});
            demoUsers.put("Elif Istanbul",     new String[]{"elif",   "Europe/Istanbul"});
            demoUsers.put("Hans Berlin",       new String[]{"hans",   "Europe/Berlin"});
            demoUsers.put("Sophie Paris",      new String[]{"sophie", "Europe/Paris"});
            demoUsers.put("Oliver London",     new String[]{"oliver", "Europe/London"});
            demoUsers.put("Ivan Moscow",       new String[]{"ivan",   "Europe/Moscow"});
            demoUsers.put("Kwame Lagos",       new String[]{"kwame",  "Africa/Lagos"});
            demoUsers.put("Amara Nairobi",     new String[]{"amara",  "Africa/Nairobi"});
            demoUsers.put("Layla Cairo",       new String[]{"layla",  "Africa/Cairo"});
            demoUsers.put("Thabo Johannesburg",new String[]{"thabo",  "Africa/Johannesburg"});
            demoUsers.put("Carlos Sao Paulo",  new String[]{"carlos", "America/Sao_Paulo"});
            demoUsers.put("Valentina Buenos Aires", new String[]{"valentina", "America/Argentina/Buenos_Aires"});
            demoUsers.put("Diego Mexico City", new String[]{"diego",  "America/Mexico_City"});
            demoUsers.put("Emily Los Angeles", new String[]{"emily",  "America/Los_Angeles"});
            demoUsers.put("James Chicago",     new String[]{"james",  "America/Chicago"});
            demoUsers.put("Liam Toronto",      new String[]{"liam",   "America/Toronto"});
            demoUsers.put("Noah Honolulu",     new String[]{"noah",   "Pacific/Honolulu"});
            demoUsers.put("Grace Sydney",      new String[]{"grace",  "Australia/Sydney"});
            demoUsers.put("Jack Perth",        new String[]{"jack",   "Australia/Perth"});
            demoUsers.put("Malia Fiji",        new String[]{"malia",  "Pacific/Fiji"});
            demoUsers.put("Nils Reykjavik",    new String[]{"nils",   "Atlantic/Reykjavik"});
            demoUsers.put("Priya Singapore",   new String[]{"priya",  "Asia/Singapore"});
            demoUsers.put("Somchai Bangkok",   new String[]{"somchai","Asia/Bangkok"});
            demoUsers.put("Linh Hanoi",        new String[]{"linh",   "Asia/Ho_Chi_Minh"});
            demoUsers.put("Juan Manila",       new String[]{"juan",   "Asia/Manila"});

            User siti = null, maya = null, alex = null;
            for (Map.Entry<String, String[]> entry : demoUsers.entrySet()) {
                String name = entry.getKey();
                String username = entry.getValue()[0];
                String timezone = entry.getValue()[1];
                User created = findOrCreateUser(users, name, username, timezone);
                if (username.equals("siti")) siti = created;
                if (username.equals("maya")) maya = created;
                if (username.equals("alex")) alex = created;
            }

            if (appointments.count() == 0) {
                Instant start1 = Instant.now().plus(1, ChronoUnit.DAYS)
                        .truncatedTo(ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS);
                appointments.save(new Appointment(
                        "Demo product sync", siti, start1, start1.plus(45, ChronoUnit.MINUTES), Set.of(maya)));

                Instant start2 = Instant.now().plus(2, ChronoUnit.DAYS)
                        .truncatedTo(ChronoUnit.DAYS).plus(9, ChronoUnit.HOURS);
                appointments.save(new Appointment(
                        "Global roadmap review", alex, start2, start2.plus(60, ChronoUnit.MINUTES),
                        Set.of(siti, maya)));
            }
        };
    }

    private User findOrCreateUser(UserRepository users, String name, String username, String timezone) {
        return users.findByUsernameIgnoreCase(username)
                .orElseGet(() -> users.save(new User(name, username, timezone)));
    }
}