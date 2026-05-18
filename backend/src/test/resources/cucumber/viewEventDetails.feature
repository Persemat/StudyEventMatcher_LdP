Feature: Visualizzazione dettagli evento
  Come utente, voglio visualizzare i dettagli di un evento così da ottenere tutte le informazioni necessarie prima di decidere se partecipare.

  Scenario: Visualizzazione dettagli evento riuscita
    Given l'utente e' autenticato per i dettagli
    And esiste un evento disponibile nel sistema
    When l'utente seleziona un evento dalla lista
    Then il sistema mostra i dettagli dell'evento
    And vengono mostrati titolo, descrizione, data, ora, luogo, tag, limite persone e stato evento

  Scenario: Visualizzazione stato prenotazione
    Given l'utente e' autenticato per i dettagli
    And l'utente e' prenotato all'evento
    When l'utente seleziona un evento dalla lista
    Then il sistema mostra che l'utente e' gia' prenotato

  Scenario: Utente non autenticato
    Given l'utente non e' autenticato per i dettagli
    When l'utente prova a visualizzare i dettagli di un evento
    Then il sistema non permette l'accesso
    And il sistema richiede l'autenticazione per i dettagli

  Scenario: Visualizzazione posti disponibili aggiornati
    Given l'utente e' autenticato per i dettagli
    And esiste un evento con un limite massimo di partecipanti
    When l'utente seleziona un evento dalla lista
    Then il sistema mostra il numero di posti disponibili
    And il numero di posti riflette correttamente le prenotazioni effettuate