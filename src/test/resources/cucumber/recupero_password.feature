Feature: Recupero password

  Scenario: Richiesta valida del codice di recupero
    Given esiste un utente registrato per il recupero password
    When l'utente richiede il codice di recupero password
    Then il sistema invia il codice di recupero

  Scenario: Richiesta con email non registrata
    Given non esiste alcun utente con quell'email per il recupero password
    When l'utente richiede il codice di recupero password
    Then il sistema rifiuta la richiesta di recupero password
    And il sistema mostra il messaggio "Nessun utente trovato con questa email"

  Scenario: Reset password con codice valido
    Given esiste un utente con un codice di recupero valido
    When l'utente conferma il reset password con un nuovo valore valido
    Then il sistema aggiorna la password

  Scenario: Reset password con codice non valido
    Given esiste un utente registrato per il reset password
    When l'utente conferma il reset password con un codice non valido
    Then il sistema rifiuta la richiesta di reset password
    And il sistema mostra il messaggio "Codice non valido"

  Scenario: Reset password con codice scaduto
    Given esiste un utente con un codice di recupero scaduto
    When l'utente conferma il reset password con un nuovo valore valido
    Then il sistema rifiuta la richiesta di reset password
    And il sistema mostra il messaggio "Codice scaduto"