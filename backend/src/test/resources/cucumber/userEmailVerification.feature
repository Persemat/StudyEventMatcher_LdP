Feature: Verifica dell'indirizzo email dell'utente

  Scenario: Invio codice di conferma dopo registrazione
    Given l'utente si trova nella pagina di registrazione
    And l'utente inserisce dati validi
    When l'utente completa la registrazione
    Then il sistema crea l'account in stato “non verificato”
    And il sistema invia un’email di conferma contenente un codice numerico a 6 cifre
    And il sistema mostra il messaggio “Registrazione completata. Controlla la tua email e inserisci il codice di conferma”

  Scenario: Conferma email completata con successo
    Given l'utente ha completato la registrazione
    And l'account è in stato “non verificato”
    And l'utente ha ricevuto un codice di conferma a 6 cifre via email
    When l'utente inserisce il codice corretto
    And conferma la verifica
    Then il sistema verifica l'indirizzo email dell'utente
    And il sistema aggiorna lo stato dell'account a “verificato”
    And il sistema mostra il messaggio “Email confermata con successo”

  Scenario: Codice di conferma non valido o già utilizzato
    Given l'utente ha ricevuto un codice di conferma a 6 cifre
    When l'utente inserisce un codice non valido o già utilizzato
    And conferma la verifica
    Then il sistema non verifica l'account
    And il sistema mostra il messaggio d'errore “Codice di conferma non valido o già utilizzato”