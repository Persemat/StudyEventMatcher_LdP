Feature: Gestione e modifica delle preferenze utente
  Come utente autenticato
  Voglio poter aggiornare i miei tag di interesse e la mia città
  Per mantenere il mio profilo aggiornato

  Background:
    Given l'utente è autenticato per la modifica preferenze
    And l'utente si trova nella pagina Profilo

  Scenario: Modifica tag con successo
    Given l'utente ha inizialmente il macro tag "Economia" e il micro tag "Finanza"
    When l'utente sostituisce i macro tag con "Informatica" e i micro tag con "Machine Learning"
    And salva le modifiche alle preferenze
    Then il sistema associa correttamente i tag "Informatica" e "Machine Learning"
    And l'operazione di aggiornamento ha successo

  Scenario: Rimozione tag/interessi con successo
    Given l'utente ha inizialmente i macro tag "Economia" e "Giurisprudenza"
    When l'utente rimuove il tag "Economia" lasciando solo "Giurisprudenza"
    And salva le modifiche alle preferenze
    Then il sistema rimuove correttamente il tag "Economia"
    And l'operazione di aggiornamento ha successo

  Scenario: Modifica città preferita con successo
    Given l'utente ha già impostato come città preferita "Roma"
    When l'utente modifica la città preferita in "Torino"
    And salva le modifiche alle preferenze
    Then l'operazione di aggiornamento ha successo
    And il sistema aggiorna correttamente la città in "Torino"

  Scenario: Tentativo di salvataggio con lista di macro tag vuota
    Given l'utente ha inizialmente il macro tag "Economia" e nessun micro tag
    When l'utente svuota la lista dei macro tag
    And salva le modifiche alle preferenze
    Then il sistema mostra l'errore "Almeno una macro area di interesse deve essere indicata" nella pagina

  Scenario: Tentativo di salvataggio con campo città vuoto
    Given l'utente ha già impostato come città preferita "Roma"
    When l'utente rimuove la città preferita senza sostituirla
    And salva le modifiche alle preferenze
    Then il sistema mostra l'errore "Bisogna indicare una città preferita" nella pagina