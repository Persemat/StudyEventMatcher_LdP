Feature: Modifica dati personali profilo

  Come utente autenticato, voglio modificare separatamente nome e cognome, email e password,
  cosi da mantenere aggiornate le informazioni del mio profilo.

  Scenario: Modifica nome e cognome con successo
    Given l'utente autenticato si trova nella pagina Profilo
    When l'utente modifica il nome da "Mario" a "Marco"
    And modifica il cognome da "Rossi" a "Bianchi"
    And salva i dati personali
    Then il sistema aggiorna correttamente i dati personali dell'utente
    And il sistema mostra il messaggio profilo di conferma "Dati personali aggiornati con successo"

  Scenario: Tentativo salvataggio dati personali con campi obbligatori vuoti
    Given l'utente autenticato si trova nella pagina Profilo
    When l'utente lascia il campo "Nome" vuoto nella modifica dati
    And salva i dati personali
    Then il sistema impedisce il salvataggio dei dati personali
    And il sistema mostra il messaggio profilo d'errore "Campi obbligatori mancanti"

  Scenario: Modifica email con successo
    Given l'utente autenticato si trova nella pagina Profilo
    When l'utente modifica la propria email in "marco@example.com"
    And salva la nuova email
    Then il sistema aggiorna correttamente l'email dell'utente
    And il sistema mostra il messaggio profilo di conferma "Dati personali aggiornati con successo"

  Scenario: Modifica password con successo
    Given l'utente autenticato si trova nella pagina Profilo
    When l'utente inserisce la password attuale corretta
    And inserisce una nuova password profilo valida
    And salva la nuova password
    Then il sistema aggiorna correttamente la password
    And il sistema mostra il messaggio profilo di conferma "Password aggiornata con successo"

  Scenario: Modifica password con password attuale errata
    Given l'utente autenticato si trova nella pagina Profilo
    When l'utente inserisce una password attuale errata
    And inserisce una nuova password profilo valida
    And salva la nuova password
    Then il sistema impedisce l'aggiornamento della password
    And il sistema mostra il messaggio profilo d'errore "Password attuale non corretta"
