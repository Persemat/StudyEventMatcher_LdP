package com.example.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity class representing an academic or social study event within the
 * platform.
 * <p>
 * It maps to the "events" database table and maintains relational links with
 * the organizing {@link User} (creator), registered attendees, and interested
 * users
 * who marked the event as a favorite.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private Double duration;

    @Column(nullable = false)
    private String location;
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @ElementCollection
    @CollectionTable(name = "event_tags", joinColumns = @JoinColumn(name = "event_id"))
    private List<EventTag> tags;

    @Column(length = 1000)
    private String description;

    private Integer maxParticipants;

    private boolean creatorParticipationRequired;

    @Column(nullable = false)
    private String status = "open";

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /**
     * Default no-argument constructor required by JPA specification.
     */
    public Event() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<EventTag> getTags() {
        return tags;
    }

    public void setTags(List<?> tags) {
        if (tags == null) {
            this.tags = null;
            return;
        }

        this.tags = new ArrayList<>(tags.stream()
                .map(tag -> {
                    if (tag instanceof EventTag eventTag) {
                        return eventTag;
                    }
                    return new EventTag(String.valueOf(tag), null);
                })
                .toList());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public boolean isCreatorParticipationRequired() {
        return creatorParticipationRequired;
    }

    public void setCreatorParticipationRequired(boolean creatorParticipationRequired) {
        this.creatorParticipationRequired = creatorParticipationRequired;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator2) {
        this.creator = creator2;
    }

    @ManyToMany
    @JoinTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "event_favorites", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> favorites = new HashSet<>();

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    /**
     * Enrolls a user into the event participant list.
     *
     * @param user the user registering for the event
     */
    public void addParticipant(User user) {
        this.participants.add(user);

    }

    /**
     * Removes a user from the event participant list.
     *
     * @param user the user cancelling their registration
     */
    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    /**
     * Computes the current total number of enrolled participants.
     *
     * @return the total count of active participants, or 0 if the list is null
     */
    public int getCurrentParticipants() {
        return participants != null ? participants.size() : 0;
    }

    public Set<User> getFavorites() {
        return favorites;
    }

    public void setFavorites(Set<User> favorites) {
        this.favorites = favorites;
    }

    /**
     * Marks this event as a favorite for a specific user.
     * <p>
     * This helper method synchronizes both sides of the bidirectional ManyToMany
     * association
     * between the event and the user's personal bookmark list.
     * </p>
     *
     * @param user the user adding the event to their favorites
     */
    public void addFavorite(User user) {
        if (this.favorites.add(user)) {
            user.addFavoriteEvent(this);
        }
    }

    /**
     * Removes this event from a specific user's favorites list.
     * <p>
     * This helper method synchronizes both sides of the bidirectional ManyToMany
     * association
     * to safely clear internal entity references.
     * </p>
     *
     * @param user the user removing the event from their bookmarks
     */
    public void removeFavorite(User user) {
        if (this.favorites.remove(user)) {
            user.removeFavoriteEvent(this);
        }
    }
}