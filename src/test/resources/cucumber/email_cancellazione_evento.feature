Feature: Notifica cancellazione evento

  Scenario: Partecipante riceve notifica quando il creatore cancella l'evento
    Given esiste un evento con creatore obbligatorio
    And l'utente è iscritto all'evento
    When il creatore cancella l'evento
    Then il sistema invia una notifica di cancellazione all'utente

  Scenario: Evento cancellato non appare più nelle prenotazioni
    Given esiste un evento con partecipanti iscritti
    And l'utente è iscritto all'evento
    When il creatore cancella l'evento
    And l'utente richiede la lista delle prenotazioni
    Then l'evento non è più presente nella lista

  Scenario: Utente non iscritto non riceve notifiche
    Given esiste un evento con partecipanti iscritti
    And un altro utente non è iscritto all'evento
    When il creatore cancella l'evento
    Then il sistema non invia notifiche a quell'utente