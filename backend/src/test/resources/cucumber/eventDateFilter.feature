@FiltroData
Feature: Filtro eventi per data

  Background:
    Given L'utente è autenticato nel sistema

  Scenario: L'utente filtra gli eventi per data con successo
    Given L'utente ha aperto il calendario dei filtri
    And sono presenti eventi in una data futura
    When seleziona una data futura in cui sono presenti eventi
    Then il sistema aggiorna la dashboard mostrando solo gli eventi programmati per quella data

  Scenario: Data senza eventi
    Given L'utente ha aperto il calendario dei filtri
    And non ci sono eventi registrati per una data specifica vuota
    When seleziona la data specifica vuota
    Then la lista degli eventi si svuota

  Scenario: Selezione di una data passata
    Given L'utente sta scegliendo una data dal calendario dei filtri
    When prova a selezionare una data passata
    Then il sistema non restituisce eventi