Feature: Visualizzazione degli eventi personali

  Scenario: Visualizzazione degli eventi personali
    Given l'utente è autenticato
    And ha eventi personali
    When l'utente visualizza la lista degli eventi personali
    Then il sistema mostra la lista degli eventi personali

  Scenario: Nessun evento personale
    Given l'utente è autenticato
    And non ha eventi personali
    When l'utente visualizza la lista degli eventi personali
    Then il sistema mostra un messaggio che indica che non ci sono eventi personali

  Scenario: Visualizzazione dei dettagli degli eventi
    Given l'utente è autenticato
    And ha eventi personali
    When l'utente visualizza la lista degli eventi personali
    Then per ogni evento personale vengono mostrati titolo, data e luogo

  Scenario: Utente non autenticato
    Given l'utente non è autenticato
    When l'utente visualizza la lista degli eventi personali
    Then il sistema impedisce l'accesso
    And il sistema richiede l'autenticazione