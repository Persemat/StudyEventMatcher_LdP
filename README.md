## Architettura e Tecnologie

Il progetto si basa su un'architettura client-server disaccoppiata. Il backend è interamente sviluppato in *Spring Boot* (Java) e gestisce la logica di business, l'algoritmo di affinità per i gruppi di studio e l'accesso al database relazionale MySQL. Il frontend è realizzato in React e comunica in modo asincrono con le API REST esposte dal server.

La gestione dell'identità, della sicurezza e dell'autorizzazione degli endpoint è implementata nativamente tramite *Spring Security*. Il nostro backend si occupa interamente del ciclo di vita dell'autenticazione: dall'hashing sicuro delle credenziali sul database fino alla generazione e validazione dei token [inserire JWT o Sessioni] necessari per accedere alle risorse protette, mantenendo il pieno controllo sulle policy di accesso senza dipendenze esterne.

Di seguito è riportato il diagramma ad alto livello che illustra il flusso dei dati e l'interazione tra i livelli (Presentation, Business Logic e Data Access) della nostra applicazione:

![Diagramma ad Alto Livello dell'Architettura](Images/Architettura_ad_alto_livello.jpeg)