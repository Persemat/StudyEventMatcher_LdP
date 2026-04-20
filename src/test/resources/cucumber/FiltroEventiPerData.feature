Feature: Filtraggio eventi per Data
  Come studente
  Voglio poter cercare gli eventi in base a una data specifica
  Per poter organizzare i miei gruppi di studio in base ai miei giorni liberi

  Background:
    Given l'utente è autenticato per la ricerca per data
    And esistono eventi pianificati per domani e per la prossima settimana

  Scenario: L'utente filtra gli eventi per una data con successo
    When cerco gli eventi specificando la data di domani
    Then il sistema restituisce solo gli eventi programmati per domani
    And non restituisce gli eventi della prossima settimana

  Scenario: Ricerca in una data senza eventi
    When cerco gli eventi in una data remota in cui non c'è nulla di programmato (es. tra 100 giorni)
    Then il sistema restituisce una lista vuota indicando che non ci sono eventi in quella data

  Scenario: Sicurezza - Chiamata API con data nel passato
    When un client aggira il frontend e cerca eventi per una data di ieri
    Then il sistema protegge i dati e restituisce una lista vuota poiché gli eventi passati sono chiusi
