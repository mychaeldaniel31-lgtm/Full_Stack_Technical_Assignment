package com.example.appointmentsystem.model;

import jakarta.persistence.*;
import java.time.ZoneId;
import java.util.UUID;
import java.util.Locale;

@Entity @Table(name="users", indexes=@Index(name="idx_user_username", columnList="username", unique=true))
public class User {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String name;
    @Column(nullable=false, unique=true) private String username;
    @Column(nullable=false) private String preferredTimezone;
    protected User() {}
    public User(String name,String username,String timezone){this.name=name;this.username=username.toLowerCase(Locale.ROOT);setPreferredTimezone(timezone);}
    public UUID getId(){return id;} public String getName(){return name;} public String getUsername(){return username;}
    public String getPreferredTimezone(){return preferredTimezone;}
    public void setPreferredTimezone(String tz){ZoneId.of(tz);this.preferredTimezone=tz;}
}
