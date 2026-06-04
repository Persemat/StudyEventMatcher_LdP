Feature: Ricerca per tag

    Scenario: Filtraggio per un singolo tag con risultati presenti
        Given sono L'utente
        And ci sono 3 eventi attivi associati a un tag specifico nel database
        When seleziono quel tag dalla lista predefinita per filtrare gli eventi
        Then il sistema deve restituire un elenco contenente esattamente quei 3 eventi
        And nessun evento privo di quel tag deve apparire nei risultati

    Scenario: Filtraggio combinato con più tag (logica OR)
        Given sono L'utente
        And esiste 1 evento associato sia al “Tag A” che al “Tag B”
        And esistono 2 eventi associati solo al “Tag A”
        When seleziono sia il “Tag A” che il “Tag B” dalla lista predefinita
        Then il sistema deve restituire gli eventi che possiedono almeno uno dei tag
        And gli eventi che possiedono uno solo dei due tag vengono inclusi nei risultati

    Scenario: Filtraggio per macrotag
        Given sono L'utente
        And esistono eventi associati a microtag dello stesso macrotag
        When seleziono solo il macrotag dalla lista predefinita
        Then il sistema deve restituire tutti gli eventi di quel macrotag

    Scenario: Nessun evento corrispondente ai tag selezionati
        Given sono L'utente
        And seleziono una combinazione di tag dalla lista predefinita
        And non esistono eventi attivi nel database che possiedano almeno uno dei tag scelti
        When il sistema elabora la richiesta di filtraggio
        Then il sistema restituisce un elenco vuoto di risultati
