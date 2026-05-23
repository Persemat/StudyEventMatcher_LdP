Feature: Notifica nuovo evento rilevante

  Scenario: Email inviata con successo
    Given l'utente ha un indirizzo email valido
    And l'utente ha tra i propri interessi il tag "Informatica > Machine Learning"
    When viene pubblicato l'evento "Introduzione al Machine Learning" con data "2026-06-10T15:00:00"
    And l'evento "Introduzione al Machine Learning" ha il tag "Informatica > Machine Learning"
    Then il sistema invia un'email all'utente
    And l'email contiene il titolo "Introduzione al Machine Learning"
    And l'email contiene la data e ora "10/06/2026 alle 15:00"

  Scenario: Email inviata solo agli utenti interessati
    Given l'utente ha un indirizzo email valido
    And l'utente ha tra i propri interessi il tag "Giurisprudenza > Diritto Privato"
    When viene pubblicato l'evento "Introduzione al Machine Learning" con data "2026-06-10T15:00:00"
    And l'evento "Introduzione al Machine Learning" ha il tag "Informatica > Machine Learning"
    Then il sistema non invia alcuna email all'utente

  Scenario: Email non duplicata per piu tag corrispondenti
    Given l'utente ha un indirizzo email valido
    And l'utente ha tra i propri interessi il tag "Informatica > Machine Learning"
    And l'utente ha tra i propri interessi il tag "Informatica > Sicurezza Informatica"
    When viene pubblicato l'evento "AI e Cybersecurity" con data "2026-06-15T14:30:00"
    And l'evento "AI e Cybersecurity" ha il tag "Informatica > Machine Learning"
    And l'evento "AI e Cybersecurity" ha il tag "Informatica > Sicurezza Informatica"
    Then il sistema invia una sola email all'utente
    And l'email contiene il titolo "AI e Cybersecurity"
