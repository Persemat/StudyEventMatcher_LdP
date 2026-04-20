Feature: Autenticazione utente

  Come utente registrato
  Voglio effettuare l'accesso
  Così da poter entrare nell'area riservata

  Scenario: Autenticazione riuscita
    Given le credenziali fornite corrispondono a un utente registrato
    When l'utente prova ad accedere
    Then il sistema riconosce l'identità dell'utente
    And permette il passaggio all'area riservata

  Scenario: Credenziali non valide
    Given l'identificativo o la password non sono corretti
    When l'utente prova ad accedere
    Then il sistema respinge la richiesta
    And non permette l'accesso all'area riservata

  Scenario: Accesso negato senza credenziali
    Given non vengono fornite credenziali
    When l'utente prova ad accedere
    Then il sistema non procede alla verifica
    And nega l'autorizzazione all'utente