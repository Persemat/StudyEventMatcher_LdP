package com.example.backend.model;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
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

    @Column(nullable = false)
    private String favouriteCity;

    @ElementCollection
    @CollectionTable(name = "user_macro_tags", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "macro_tag", nullable = false)
    private List<String> macroTags;

    @ElementCollection
    @CollectionTable(name = "user_micro_tags", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "micro_tag")
    private List<String> microTags;

    // MANY TO MANY RELATION
    @ManyToMany(mappedBy = "participants")
    private Set<Event> bookedEvents = new HashSet<>();

    public User() {
    }

    public User(String name, String lastname, String email, String password, String favouriteCity,
            List<String> macroTags, List<String> microTags) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.favouriteCity = favouriteCity;
        this.macroTags = macroTags;
        this.microTags = microTags;
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

    public void setFavouriteCity(String favouriteCity) {
        this.favouriteCity = favouriteCity;
    }

    public String getFavouriteCity() {
        return this.favouriteCity;
    }

    public void setMacroTags(List<String> macroTags) {
        this.macroTags = macroTags;
    }

    public List<String> getMacroTags() {
        return this.macroTags;
    }

    public void setMicroTags(List<String> microTags) {
        this.microTags = microTags;
    }

    public List<String> getMicroTags() {
        return this.microTags;
    }
}