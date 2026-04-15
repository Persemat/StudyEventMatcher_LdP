Feature: Modifica evento

  Scenario: Modifica con successo
    Given l'utente è autenticato
    And esiste un evento "Progetto Java" creato dall'utente
    And l'evento inizia tra più di un giorno
    When l'utente modifica l'orario di inizio dell'evento
    And salva le modifiche
    Then il sistema aggiorna l'evento correttamente
    And il sistema mostra il messaggio "Evento 'Progetto Java' modificato con successo!"

  Scenario: Tentativo salvataggio con campi vuoti
    Given l'utente è autenticato
    And esiste un evento "Progetto Java" creato dall'utente
    When l'utente lascia il campo titolo vuoto
    And salva le modifiche
    Then il sistema impedisce il salvataggio della modifica
    And il sistema mostra il messaggio di errore "Campi obbligatori mancanti"

  Scenario: Utente non autorizzato
    Given l'utente è autenticato
    And esiste un evento "Progetto Java" creato da un altro utente
    When l'utente visualizza gli eventi disponibili
    Then il pulsante modifica evento non è visibile