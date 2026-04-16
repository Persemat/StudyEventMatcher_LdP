Feature: Cancellazione prenotazione

    Scenario: L'utente cancella la propria prenotazione
        Given l'utente è autenticato
        And l'utente ha una prenotazione per un evento
        When l'utente prova a cancellare la prenotazione
        Then il sistema rimuove la prenotazione
        And il sistema mostra il messaggio di cancellazione "Prenotazione cancellata con successo"

    Scenario: L'utente tenta di cancellare una prenotazione inesistente
        Given l'utente è autenticato
        And l'utente non è prenotato a quell'evento
        When l'utente prova a cancellare la prenotazione
        Then il sistema impedisce l'operazione
        And il sistema mostra il messaggio di cancellazione "Errore: Non sei prenotato a questo evento"

    Scenario: Utente non autenticato tenta la cancellazione
        Given l'utente non è autenticato per la cancellazione
        When l'utente prova a cancellare la prenotazione
        Then il sistema impedisce l'operazione
        And il sistema chiede l'autenticazione

    Scenario: Il posto viene liberato dopo la cancellazione
        Given l'utente è autenticato
        And l'utente ha una prenotazione per un evento con posti limitati
        When l'utente cancella la prenotazione
        Then il sistema aumenta il numero di posti disponibili