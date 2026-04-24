Feature: Registrazione utente

  Come utente
  Voglio creare un account
  Così da poter accedere alla piattaforma

  Scenario: Registrazione con credenziali valide
    Given l'utente non è registrato
    And l'utente non è autenticato
    And inserisce un'email con formato valido
    And l'email non è associata ad altri account
    And inserisce una password con formato corretto
    When l'utente prova a registrarsi
    Then il sistema registra l’utente
    And il sistema mostra l'email dell'utente registrato

  Scenario: Registrazione con email già associata a un account
    Given l'utente non è autenticato
    And l'email è già associata a un account esistente
    When l'utente prova a registrarsi
    Then il sistema impedisce la registrazione
    And il sistema mostra "Email già registrata"

  Scenario: Registrazione con formato email non valido
    Given l'utente non è registrato
    And inserisce un'email con formato non valido
    When l'utente prova a registrarsi
    Then il sistema impedisce la registrazione
    And il sistema mostra "Email non valida"

  Scenario: Registrazione con formato password non valido
    Given l'utente non è registrato
    And inserisce una password con formato non valido
    When l'utente prova a registrarsi
    Then il sistema impedisce la registrazione
    And il sistema mostra "Password troppo corta"