package com.dem.ldp.service;

import com.dem.ldp.dto.ForgotPasswordRequest;
import com.dem.ldp.dto.ResetPasswordRequest;
import com.dem.ldp.model.PasswordResetToken;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.PasswordResetTokenRepository;
import com.dem.ldp.repository.UtenteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    private final UtenteRepository utenteRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(UtenteRepository utenteRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.utenteRepository = utenteRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.email();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obbligatoria");
        }

        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nessun utente trovato con questa email"));

        LocalDateTime adesso = LocalDateTime.now();

        // Pulizia dei token scaduti
        passwordResetTokenRepository.deleteByScadenzaBefore(adesso);

        // Evita richieste troppo ravvicinate
        PasswordResetToken ultimoToken = passwordResetTokenRepository
                .findTopByEmailOrderByCreatoIlDesc(email)
                .orElse(null);

        if (ultimoToken != null) {
            long secondiPassati = java.time.Duration
                    .between(ultimoToken.getCreatoIl(), adesso)
                    .getSeconds();

            if (secondiPassati < 30) {
                throw new IllegalArgumentException("Attendi almeno 30 secondi prima di richiedere un nuovo codice");
            }
        }

        // Se richiede un nuovo codice, elimino quelli vecchi
        passwordResetTokenRepository.deleteByEmail(email);

        String codice = generaCodiceReset();

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(utente.getEmail());
        token.setCodice(codice);
        token.setCreatoIl(adesso);
        token.setScadenza(adesso.plusMinutes(10));
        token.setUsato(false);

        passwordResetTokenRepository.save(token);

        emailService.inviaCodiceRecuperoPassword(utente.getEmail(), codice);

        return "Codice di recupero inviato via email";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        String email = request.email();
        String codice = request.codice();
        String nuovaPassword = request.nuovaPassword();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obbligatoria");
        }

        if (codice == null || codice.isBlank()) {
            throw new IllegalArgumentException("Codice obbligatorio");
        }

        if (nuovaPassword == null || nuovaPassword.length() < 8) {
            throw new IllegalArgumentException("Password troppo corta");
        }

        if (!nuovaPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Serve una maiuscola");
        }

        if (!nuovaPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Serve un numero");
        }

        PasswordResetToken token = passwordResetTokenRepository
                .findByEmailAndCodiceAndUsatoFalse(email, codice)
                .orElseThrow(() -> new IllegalArgumentException("Codice non valido"));

        if (token.getScadenza().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(token);
            throw new IllegalArgumentException("Codice scaduto");
        }

        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        utente.setPassword(passwordEncoder.encode(nuovaPassword));
        utenteRepository.save(utente);

        // Dopo l'uso il token non serve più
        passwordResetTokenRepository.delete(token);

        return "Password aggiornata con successo";
    }

    private String generaCodiceReset() {
        int numero = 100000 + new Random().nextInt(900000);
        return String.valueOf(numero);
    }
}