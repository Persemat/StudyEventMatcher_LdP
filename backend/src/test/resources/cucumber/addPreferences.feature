Feature: Registrazione Utente con Preferenze
  Come nuovo utente
  Voglio registrarmi alla piattaforma indicando la mia città e i miei interessi
  Per ricevere suggerimenti personalizzati

  Background:
    Given il database utenti è pulito

  Scenario: Registrazione completata con successo con città e interessi
    When inserisco nome "Mario", cognome "Rossi", email "mario@example.com" e password "Password123!"
    And scelgo "Milano" come città preferita
    And scelgo "Informatica" come macro argomento di interesse
    And scelgo i micro argomenti "Informatica > Machine Learning" e "Matematica > Analisi"
    And confermo la registrazione
    Then il mio account deve essere creato correttamente
    And nel mio profilo la città deve essere "Milano"
    And i macro tag devono contenere "Informatica"
    And i micro tag devono contenere "Informatica > Machine Learning" e "Matematica > Analisi"

  Scenario: Registrazione completata con successo con solo città e macro tag
    When inserisco nome "Mario", cognome "Rossi", email "mario@example.com" e password "Password123!"
    And scelgo "Milano" come città preferita
    And scelgo "Informatica" come macro argomento di interesse
    And non scelgo alcun micro argomento di interesse
    And confermo la registrazione
    Then il mio account deve essere creato correttamente
    And i micro tag salvati devono rimanere vuoti

  Scenario: Tentativo di registrazione senza inserire la città
    When inserisco nome "Mario", cognome "Rossi", email "mario@example.com" e password "Password123!"
    And lascio vuoto il campo città preferita
    And scelgo "Informatica" come interesse
    And provo a confermare la registrazione
    Then il sistema deve mostrare l'errore "Bisogna indicare una città preferita"
    And nessun nuovo account deve essere creato

  Scenario: Tentativo di registrazione senza inserire nemmeno un macro tag
    When inserisco nome "Mario", cognome "Rossi", email "mario@example.com" e password "Password123!"
    And scelgo "Milano" come città preferita
    And non seleziono nessun macro argomento di interesse
    And provo a confermare la registrazione
    Then il sistema deve mostrare l'errore "Almeno una macro area di interesse deve essere indicata"
    And nessun nuovo account deve essere creato