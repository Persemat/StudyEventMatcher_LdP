Feature: Cancellazione di un evento creato

  Scenario: L'utente cancella un proprio evento con presenza obbligatoria del creatore
    Given l'utente è il creatore di un evento
    And l’evento ha il flag di presenza obbligatoria del creatore attivo
    And l'evento ha 5 prenotazioni associate nel database
    When prova a cancellare l'evento
    Then dovrebbe ricevere un messaggio di conferma di avvenuta cancellazione
    And il record dell'evento viene rimosso dal database
    And le 5 prenotazioni associate vengono eliminate a cascata

  Scenario: L'utente cancella un proprio evento senza presenza obbligatoria del creatore e con altri iscritti
    Given l'utente è il creatore di un evento
    And l’evento ha il flag di presenza obbligatoria del creatore disattivo
    And l’evento ha altri utenti iscritti
    When prova a cancellare l'evento
    Then dovrebbe ricevere un messaggio di conferma di avvenuta cancellazione
    And il creatore viene rimosso dall’evento
    And l’evento rimane presente nel database
    And le prenotazioni degli altri utenti rimangono attive

  Scenario: L'utente cancella un proprio evento senza presenza obbligatoria del creatore e senza altri iscritti
    Given l'utente è il creatore di un evento
    And l’evento ha il flag di presenza obbligatoria del creatore disattivo
    And l’evento non ha altri utenti iscritti
    When prova a cancellare l'evento
    Then dovrebbe ricevere un messaggio di conferma di avvenuta cancellazione
    And il record dell'evento viene rimosso dal database

  Scenario: Riassegnazione del creatore
    Given l'utente è il creatore di un evento
    And l’evento ha il flag di presenza obbligatoria del creatore disattivo
    And esistono altri utenti iscritti all’evento
    When prova a cancellare l'evento
    Then il creatore viene rimosso dall’evento
    And il sistema assegna il ruolo di creatore al primo utente iscritto
    And l’evento rimane presente nel database