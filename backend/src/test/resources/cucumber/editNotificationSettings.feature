Feature: Gestione delle impostazioni di notifica email

  Background:
    Given l'utente è autenticato e nella sezione impostazioni notifiche

  Scenario: Attivazione notifiche email per nuove pubblicazioni
    Given le notifiche email per nuove pubblicazioni relative agli interessi/tag sono disattivate
    When l'utente attiva l'opzione notifiche email per nuove pubblicazioni
    And salva le modifiche alle impostazioni notifiche
    Then il sistema aggiorna correttamente la preferenza nel database

  Scenario: Disattivazione notifiche email per modifica evento
    Given le notifiche email per modifica evento sono attivate
    When l'utente disattiva l'opzione notifiche email per modifica evento
    And salva le modifiche alle impostazioni notifiche
    Then il sistema aggiorna correttamente la preferenza nel database

  Scenario: Disattivazione notifiche email per cancellazione evento
    Given le notifiche email per cancellazione evento sono attivate
    When l'utente disattiva l'opzione notifiche email per cancellazione evento
    And salva le modifiche alle impostazioni notifiche
    Then il sistema aggiorna correttamente la preferenza nel database