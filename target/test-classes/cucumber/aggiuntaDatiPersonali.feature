Feature: Aggiunta dati personali utente

  Come utente, voglio inserire i miei dati personali al momento della registrazione,
  così da poter completare il mio profilo utente

  Scenario: Inserimento corretto dei dati obbligatori
    Given l'utente è nella schermata dei dati personali
    When l'utente compila tutti i campi obbligatori con dati validi
    And tenta di continuare nella registrazione
    Then il sistema salva i dati

  Scenario: Tentativo di proseguire con campi obbligatori vuoti
    Given l'utente è nella schermata dei dati personali
    When l'utente lascia vuoto un campo obbligatorio (es. "Cognome")
    And tenta di continuare nella registrazione
    Then il sistema blocca l'avanzamento
    And evidenzia il campo mancante con un messaggio



