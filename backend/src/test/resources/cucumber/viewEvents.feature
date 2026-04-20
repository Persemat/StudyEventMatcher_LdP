Feature: Visualizzazione iniziale degli eventi

  Scenario: Visualizzazione iniziale degli eventi
    Given l'utente si trova nella pagina degli eventi
    And sono presenti eventi nel sistema
    When accede alla pagina degli eventi
    Then il sistema mostra la lista degli eventi disponibili
    And gli eventi sono visualizzati senza filtri applicati
    And per ogni evento vengono mostrati titolo, data e luogo

  Scenario: Nessun evento disponibile
    Given l'utente si trova nella pagina degli eventi
    And non sono presenti eventi nel sistema
    When accede alla pagina degli eventi
    Then il sistema non mostra eventi
    And il sistema restituisce una lista vuota