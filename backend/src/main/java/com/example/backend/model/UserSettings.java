package com.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

/**
 * Entity class representing a user's communication and email notification
 * preferences.
 * <p>
 * It maintains configuration flags that determine whether a user should receive
 * automated
 * email alerts regarding event modifications, event cancellations, or
 * personalized
 * recommendations for newly published events.
 * </p>
 */
@Entity
public class UserSettings {

    @Id
    private String email;

    @Column(nullable = false)
    private boolean editEventEmailActivated;

    @Column(nullable = false)
    private boolean deleteEventEmailActivated;

    @Column(nullable = false)
    private boolean recomendedEventEmailActivated;

    /**
     * Default no-argument constructor required by JPA specification.
     */
    public UserSettings() {
    }

    /**
     * Constructs a new UserSettings profile initialized with a target email
     * identifier
     * and specific operational notification flags.
     *
     * @param email                         the unique email address acting as the
     *                                      primary key identifier
     * @param editEventEmailActivated       flag specifying if emails should be sent
     *                                      when events are modified
     * @param deleteEventEmailActivated     flag specifying if emails should be sent
     *                                      when events are cancelled
     * @param recomendedEventEmailActivated flag specifying if emails should be sent
     *                                      for relevant recommendations
     */
    public UserSettings(String email, boolean editEventEmailActivated,
            boolean deleteEventEmailActivated, boolean recomendedEventEmailActivated) {
        this.email = email;
        this.editEventEmailActivated = editEventEmailActivated;
        this.deleteEventEmailActivated = deleteEventEmailActivated;
        this.recomendedEventEmailActivated = recomendedEventEmailActivated;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEditEventEmailActivated() {
        return this.editEventEmailActivated;
    }

    public void setEditEventEmailActivated(boolean editEventEmailActivated) {
        this.editEventEmailActivated = editEventEmailActivated;
    }

    public boolean isDeleteEventEmailActivated() {
        return this.deleteEventEmailActivated;
    }

    public void setDeleteEventEmailActivated(boolean deleteEventEmailActivated) {
        this.deleteEventEmailActivated = deleteEventEmailActivated;
    }

    public boolean isRecomendedEventEmailActivated() {
        return this.recomendedEventEmailActivated;
    }

    public void setRecomendedEventEmailActivated(boolean recomendedEventEmailActivated) {
        this.recomendedEventEmailActivated = recomendedEventEmailActivated;
    }

}
