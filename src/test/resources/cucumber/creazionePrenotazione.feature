Feature: Prenotazione evento

  Scenario: Prenotazione con successo
    Given esiste un utente autenticato per la prenotazione
    And esiste un evento aperto e non al completo
    When l'utente prenota il posto per quell'evento
    Then il sistema registra la prenotazione all'evento
    And il sistema mostra il messaggio di successo della prenotazione

  Scenario: Evento al completo
    Given esiste un utente autenticato per la prenotazione
    And esiste un evento aperto ma al completo
    When l'utente prenota il posto per quell'evento
    Then il sistema impedisce la prenotazione per evento pieno
    And il sistema mostra il messaggio di evento al completo

  Scenario: Utente già prenotato a un evento
    Given esiste un utente autenticato per la prenotazione
    And l'utente è già prenotato a un evento
    When l'utente visualizza l'evento
    Then il sistema non mostra il pulsante "prenota"
    And il sistema mostra il pulsante "annulla prenotazione"