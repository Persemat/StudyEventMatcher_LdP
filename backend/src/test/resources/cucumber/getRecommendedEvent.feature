Feature: Visualizzazione eventi consigliati nella dashboard
  Come utente, voglio visualizzare nella mia dashboard eventi consigliati in base ai miei tag/interessi preferiti e alla mia posizione, così da trovare più facilmente eventi rilevanti e vicini a me.

  Scenario: Visualizzazione eventi consigliati per tag e posizione con singolo tag
    Given l'utente è autenticato
    And l'utente ha tra i propri interessi il solo macro tag "Informatica"
    And l'utente ha una città preferita salvata
    And esistono eventi futuri con il tag "Informatica" ad una distanza inferiore alla distanza limite
    When l'utente accede alla dashboard
    Then il sistema mostra una sezione Eventi consigliati per te
    And gli eventi sono ordinati in base alla distanza dalla posizione di riferimento salvata

  Scenario: Ordine corretto tra eventi con stessa distanza: evento con più tag associati
    Given l'utente è autenticato
    And l'utente ha tra i propri interessi il solo macro tag "Informatica" e i micro tag "Microeconomia" e "Diritto Privato"
    And l'utente ha una città preferita salvata
    And esistono due eventi futuri, evento 1 con solo "Microeconomia" ed evento 2 con "Microeconomia" e "Diritto Privato"
    And entrambi si svolgono alla stessa distanza
    When l'utente accede alla dashboard
    Then il sistema mostra entrambi gli eventi nella sezione Eventi consigliati per te
    And evento 2 viene visualizzato prima di evento 1 nell'ordine finale

  Scenario: Ordine corretto tra eventi con stessa distanza: macro tag vs micro tag
    Given l'utente è autenticato
    And l'utente ha tra i propri interessi il solo macro tag "Informatica" e i micro tag "Microeconomia" e "Diritto Privato"
    And l'utente ha una città preferita salvata
    And esistono due eventi futuri, evento 1 con solo "Microeconomia" ed evento 2 con solo "Informatica"
    And entrambi si svolgono alla stessa distanza
    When l'utente accede alla dashboard
    Then il sistema mostra entrambi gli eventi nella sezione Eventi consigliati per te
    And evento 1 viene visualizzato prima di evento 2 nell'ordine finale

  Scenario: Visualizzazione eventi consigliati con solo eventi non affini
    Given l'utente è autenticato
    And l'utente ha tra i propri interessi il solo macro tag "Informatica"
    And l'utente ha una città preferita salvata
    And non esistono eventi futuri affini
    When l'utente accede alla dashboard
    Then il sistema mostra il messaggio "Nessun evento corrispondente ai tuoi interessi, prova a modificarli o usa la funzione di ricerca generale."

  Scenario: Visualizzazione eventi consigliati oltre la distanza massima
    Given l'utente è autenticato
    And l'utente ha tra i propri interessi il solo macro tag "Informatica"
    And l'utente ha una città preferita salvata
    And esistono eventi affini ma oltre la distanza limite
    When l'utente accede alla dashboard
    Then il sistema mostra il messaggio "NO_LOCATION_MATCH"