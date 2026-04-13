Feature: Logout utente

    Come utente, voglio poter uscire dal mio account, così da disconnettermi dalla piattaforma

    Scenario: Disconnessione riuscita
        Given l'utente è autenticato
        And l'utente ha una sessione attiva
        When l'utente richiede il logout
        Then il sistema termina la sessione
        And il sistema invalida i token di autenticazione
        And il sistema revoca l’accesso alle aree riservate

    Scenario: Utente non autenticato prova a fare logout
        Given l'utente non è autenticato per il logout
        When l'utente prova a effettuare il logout
        Then il sistema non esegue alcuna operazione
        And il sistema mantiene lo stato di utente anonimo