Feature: Filtraggio eventi per Luogo
  Come studente
  Voglio poter cercare gli eventi in base alla città o al luogo
  Per trovare facilmente i gruppi di studio vicini a me

  Background:
    Given l'utente è autenticato per la ricerca per luogo
    And esistono i seguenti eventi registrati nel sistema:
      | Titolo           | Luogo           |
      | Gruppo Analisi   | Milano - U4     |
      | Ripasso Java     | Milano - U14    |
      | Studio Fisica    | Torino - Poli   |

  Scenario: L'utente filtra gli eventi per luogo con successo
    When cerco gli eventi specificando il luogo "Milano"
    Then il sistema restituisce 2 eventi
    And tutti gli eventi restituiti si svolgono a "Milano"

  Scenario: Nessun evento trovato nel luogo specificato
    When cerco gli eventi specificando il luogo "Roma"
    Then il sistema restituisce una lista vuota indicando che non ci sono eventi

  Scenario: Input non valido o vuoto ignora il filtro
    When cerco gli eventi lasciando il filtro luogo vuoto
    Then il sistema ignora il filtro e restituisce tutti i 3 eventi disponibili