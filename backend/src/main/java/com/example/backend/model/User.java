package com.example.backend.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name= "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // 🔥 MANY TO MANY RELATION
    @ManyToMany(mappedBy = "participants")
    private Set<Event> bookedEvents = new HashSet<>();

    public User() {}

    public User(String name, String lastname, String email, String password) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    // GETTERS & SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // 🔹 RELATION GETTERS/SETTERS
    public Set<Event> getBookedEvents() {
        return bookedEvents;
    }

    public void setBookedEvents(Set<Event> bookedEvents) {
        this.bookedEvents = bookedEvents;
    }

    public void addEvent(Event event) {
        bookedEvents.add(event);
        event.getParticipants().add(this);
    }

    public void removeEvent(Event event) {
        bookedEvents.remove(event);
        event.getParticipants().remove(this);
    }
}