package com.example.appointmentsystem.model;

import jakarta.persistence.*;
import java.time.Instant; import java.util.*;

@Entity @Table(name="appointments", indexes={@Index(name="idx_appt_start",columnList="start_time"),@Index(name="idx_appt_creator",columnList="creator_id")})
public class Appointment {
 @Id @GeneratedValue(strategy=GenerationType.UUID)
 private UUID id;
 @Column(nullable=false)
 private String title;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)
 private User creator;
 @Column(name="start_time",nullable=false)
 private Instant start;
 @Column(name="end_time",nullable=false)
 private Instant end;

 @ManyToMany(fetch=FetchType.LAZY)
 @JoinTable(name="appointment_invitees",joinColumns=@JoinColumn(name="appointment_id"),inverseJoinColumns=@JoinColumn(name="user_id"),indexes=@Index(name="idx_invitee_user",columnList="user_id")) private Set<User> invitees=new HashSet<>();
 protected Appointment() {}

 public Appointment(String title,User creator,Instant start,Instant end,Set<User> invitees){this.title=title;this.creator=creator;this.start=start;this.end=end;this.invitees=invitees;}

 public UUID getId(){return id;} public String getTitle(){return title;} public User getCreator(){return creator;} public Instant getStart(){return start;} public Instant getEnd(){return end;} public Set<User> getInvitees(){return invitees;}
}
