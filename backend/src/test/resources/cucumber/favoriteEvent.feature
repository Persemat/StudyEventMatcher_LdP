Feature: Gestione eventi preferiti

  Scenario: Aggiunta ai preferiti
    Given l’utente sta visualizzando i dettagli di un evento
    When l’utente clicca il pulsante "Aggiungi ai preferiti"
    Then il sistema inserisce l’evento nella sezione personale "I miei preferiti"

  Scenario: Rimozione dai preferiti
    Given l’utente si trova nella sezione "I miei preferiti"
    And l’evento "Progetto Java" è presente nell’elenco
    When l’utente clicca il pulsante "Rimuovi dai preferiti"
    Then l’evento scompare immediatamente dall’elenco

  Scenario: Evento preferito cancellato
    Given l’utente ha salvato nell’elenco degli eventi preferiti l’evento "Progetto Java"
    When il creatore dell’evento "Progetto Java" cancella l’evento dalla piattaforma
    Then il sistema rimuove automaticamente l'evento anche dalla lista dei preferiti dell'utente