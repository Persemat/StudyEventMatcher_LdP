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

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Required fields
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private Double duration;

    @Column(nullable = false)
    private String location;

    @ElementCollection
    @CollectionTable(name = "event_tags", joinColumns = @JoinColumn(name = "event_id"))
    private List<EventTag> tags;

    // Optional fields
    @Column(length = 1000)
    private String description;

    private Integer maxParticipants;

    private boolean creatorParticipationRequired;

    @Column(nullable = false)
    private String status = "open";

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // Empty constructor
    public Event() {}

    // ===================== GETTERS & SETTERS =====================

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

    // ===================== PARTICIPANTS =====================

    @ManyToMany
    @JoinTable(
        name = "event_participants",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();




    @ManyToMany(mappedBy = "favorites")
    private Set<User> favoritedBy = new HashSet<>();





    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public void addParticipant(User user) {
        this.participants.add(user);
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    public int getCurrentParticipants() {
        return participants != null ? participants.size() : 0;
    }

    public Set<User> getFavoritedBy() {
    return favoritedBy;
}

    public void setFavoritedBy(Set<User> favoritedBy) {
        this.favoritedBy = favoritedBy;
    }
}
