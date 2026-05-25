package com.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;


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

    public UserSettings() {
    }

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
