package com.dem.ldp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String codice;

    @Column(nullable = false)
    private LocalDateTime scadenza;

    @Column(nullable = false)
    private boolean usato = false;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String email, String codice, LocalDateTime scadenza) {
        this.email = email;
        this.codice = codice;
        this.scadenza = scadenza;
        this.usato = false;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getCodice() {
        return codice;
    }

    public LocalDateTime getScadenza() {
        return scadenza;
    }

    public boolean isUsato() {
        return usato;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public void setScadenza(LocalDateTime scadenza) {
        this.scadenza = scadenza;
    }

    public void setUsato(boolean usato) {
        this.usato = usato;
    }
}