package com.example.backend.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Entity class representing a platform user account.
 * <p>
 * It maps to the "users" database table and holds personal profile information,
 * local interest tags, and handles associations with events the user has booked
 * or bookmarked.
 */
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

    @ManyToMany(mappedBy = "participants")
    private Set<Event> bookedEvents = new HashSet<>();

    @ManyToMany(mappedBy = "favorites")
    private Set<Event> favoriteEvents = new HashSet<>();

    /**
     * Default no-argument constructor required by JPA specification.
     */
    public User() {
    }

    /**
     * Constructs a full User profile with interest preferences.
     *
     * @param name          the first name of the user
     * @param lastname      the last name or surname of the user
     * @param email         the unique email address acting as account identity
     * @param password      the hashed password string
     * @param favouriteCity the user's preferred city geographical filter
     * @param macroTags     the list of general interest categories
     * @param microTags     the list of specific targeted interest topics
     */
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

    public String getFavouriteCity() {
        return favouriteCity;
    }

    public void setFavouriteCity(String favouriteCity) {
        this.favouriteCity = favouriteCity;
    }

    public List<String> getMacroTags() {
        return macroTags;
    }

    public void setMacroTags(List<String> macroTags) {
        this.macroTags = macroTags;
    }

    public List<String> getMicroTags() {
        return microTags;
    }

    public void setMicroTags(List<String> microTags) {
        this.microTags = microTags;
    }

    public Set<Event> getBookedEvents() {
        return bookedEvents;
    }

    public void setBookedEvents(Set<Event> bookedEvents) {
        this.bookedEvents = bookedEvents;
    }

    /**
     * Adds an event to the user's personal bookmarks list.
     * <p>
     * This helper method synchronizes both sides of the bidirectional ManyToMany
     * association
     * between the user and the event favorite mappings.
     * </p>
     *
     * @param event the event to mark as favorite
     */
    public void addFavoriteEvent(Event event) {
        if (this.favoriteEvents.add(event)) {
            event.addFavorite(this);
        }
    }

    /**
     * Removes an event from the user's personal bookmarks list.
     * <p>
     * This helper method synchronizes both sides of the bidirectional ManyToMany
     * association
     * to safely clear internal entity references.
     * </p>
     *
     * @param event the event to unmark as favorite
     */
    public void removeFavoriteEvent(Event event) {
        if (this.favoriteEvents.remove(event)) {
            event.removeFavorite(this);
        }
    }

    public Set<Event> getFavoriteEvents() {
        return favoriteEvents;
    }

    public void setFavoriteEvents(Set<Event> favoritedBy) {
        this.favoriteEvents = favoritedBy;
    }
}