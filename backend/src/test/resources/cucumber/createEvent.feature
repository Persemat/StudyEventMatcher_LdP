Feature: Creazione di un nuovo evento
  Come utente, voglio creare un evento di studio o progetto inserendo 
  tutte le informazioni necessarie così da renderlo completo e disponibile agli altri utenti.

  Scenario: Creazione di un evento valido
    Given l'utente si trova nella pagina di creazione evento
    When l'utente inserisce titolo, descrizione, data, durata e luogo
    And aggiunge uno o piu tag e imposta un limite di partecipanti
    And imposta il flag Partecipazione del creatore necessaria
    And conferma la creazione
    Then il sistema salva il nuovo evento
    And il sistema imposta lo stato dell'evento come aperto
    And l'evento appare nella lista degli eventi

  Scenario: Tentativo di creazione con campi mancanti
    Given l'utente si trova nella pagina di creazione evento
    When l'utente tenta di creare un evento senza compilare i campi obbligatori
    Then il sistema impedisce la creazione dell'evento
    And il sistema mostra l'errore "Campi obbligatori mancanti"

    Scenario: Tentativo di creazione di un evento duplicato
    Given l'utente si trova nella pagina di creazione evento
    And l'utente ha gia' creato un evento nel sistema
    When l'utente compila i campi con gli stessi dati esatti dell'evento esistente
    And conferma la creazione
    Then il sistema impedisce la creazione dell'evento
    And il sistema mostra l'errore "Un evento identico esiste gia"