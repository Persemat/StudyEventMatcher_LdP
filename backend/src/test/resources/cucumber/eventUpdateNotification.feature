Feature: Notifica modifica evento

  Scenario: L'utente iscritto riceve un'email quando l'evento viene modificato
    Given esiste l'evento "Progetto Java" con data "2026-05-20T15:00:00" e luogo "Biblioteca"
    And l'utente è iscritto all'evento "Progetto Java"
    When il creatore modifica il luogo dell'evento "Progetto Java" in "Aula 3"
    Then il sistema invia un'email all'utente iscritto
    And l'email contiene il messaggio "L'evento Progetto Java è stato aggiornato"

  Scenario: Un utente non iscritto non riceve email
    Given esiste l'evento "Progetto Java" con data "2026-05-20T15:00:00" e luogo "Biblioteca"
    And un utente non è iscritto all'evento "Progetto Java"
    When il creatore modifica il luogo dell'evento "Progetto Java" in "Aula 3"
    Then il sistema non invia alcuna email a quell'utente

  Scenario: L'utente visualizza che l'evento è stato aggiornato
    Given esiste l'evento "Progetto Java" con data "2026-05-20T15:00:00" e luogo "Biblioteca"
    And l'utente è iscritto all'evento "Progetto Java"
    When il creatore modifica il luogo dell'evento "Progetto Java" in "Aula 3"
    And l'utente visualizza i dettagli dell'evento "Progetto Java"
    Then il sistema mostra il luogo aggiornato "Aula 3"
