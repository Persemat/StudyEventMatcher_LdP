package com.example.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetRecommendedEvents {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private Long userId;
    private User testUser;
    private User creatorUser;

    // Coordinate simulate per la città preferita dell'utente ("Milano")
    private final Double USER_LAT = 45.0;
    private final Double USER_LON = 9.0;

    @Given("l'utente è autenticato")
    public void utenteAutenticato() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Utente che fa la ricerca
        testUser = new User("Mario", "Rossi", "mario@example.com", "Password123!", "Milano", new ArrayList<>(),
                new ArrayList<>());
        testUser = userRepository.save(testUser);
        userId = testUser.getId();

        // Un altro utente per creare gli eventi (così non vengono scartati perché
        // creati da noi)
        creatorUser = new User("Luigi", "Verdi", "luigi@example.com", "Password123!", "Roma", new ArrayList<>(),
                new ArrayList<>());
        creatorUser = userRepository.save(creatorUser);
    }

    @And("l'utente ha una città preferita salvata")
    public void utenteHaCittaPreferita() {
        // Già impostata su "Milano" nello step precedente.
        // L'API riceverà le coordinate tramite i @RequestParam.
    }

    @And("l'utente ha tra i propri interessi il solo macro tag {string}")
    public void utenteHaSoloMacroTag(String macroTag) {
        testUser.setMacroTags(List.of(macroTag));
        userRepository.save(testUser);
    }

    @And("l'utente ha tra i propri interessi il solo macro tag {string} e i micro tag {string} e {string}")
    public void utenteHaMacroEMicroTags(String macro, String micro1, String micro2) {
        testUser.setMacroTags(List.of(macro));
        testUser.setMicroTags(List.of(micro1, micro2));
        userRepository.save(testUser);
    }

    @And("esistono eventi futuri con il tag {string} ad una distanza inferiore alla distanza limite")
    public void esistonoEventiSingoloTag(String macroTag) {
        // Evento 1 a 10km, Evento 2 a 30km
        creaEventoNelDB("Evento Vicino", macroTag, null, 45.05, 9.05);
        creaEventoNelDB("Evento Lontano", macroTag, null, 45.20, 9.20);
    }

    @And("esistono due eventi futuri, evento 1 con solo {string} ed evento 2 con {string} e {string}")
    public void esistonoDueEventiPerDoppioMatch(String micro1, String repMicro1, String micro2) {
        // Stessa identica distanza
        creaEventoNelDB("Evento 1", "Economia", micro1, 45.1, 9.1);
        Event e2 = creaEventoNelDB("Evento 2", "Economia", micro1, 45.1, 9.1);
        e2.getTags().add(new EventTag("Giurisprudenza", micro2));
        eventRepository.save(e2);
    }

    @And("esistono due eventi futuri, evento 1 con solo {string} ed evento 2 con solo {string}")
    public void esistonoDueEventiMicroVsMacro(String microTag, String macroTag) {
        creaEventoNelDB("Evento 1", "Economia", microTag, 45.1, 9.1);
        creaEventoNelDB("Evento 2", macroTag, null, 45.1, 9.1);
    }

    @And("entrambi si svolgono alla stessa distanza")
    public void entrambiStessaDistanza() {
        // Gestito implicitamente nella creazione degli eventi sopra (stesse coordinate)
    }

    @And("non esistono eventi futuri affini")
    public void nonEsistonoEventiAffini() {
        creaEventoNelDB("Evento Cucina", "Cucina", "Sushi", 45.0, 9.0);
    }

    @And("esistono eventi affini ma oltre la distanza limite")
    public void eventiOltreDistanzaLimite() {
        // Distanza enorme (> 300km)
        creaEventoNelDB("Evento Lontanissimo", "Informatica", null, 48.0, 9.0);
    }

    @When("l'utente accede alla dashboard")
    public void utenteAccedeAllaDashboard() throws Exception {
        result = mockMvc.perform(get("/events/recommended")
                .sessionAttr("userId", userId)
                .param("latitude", USER_LAT.toString())
                .param("longitude", USER_LON.toString())
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Then("il sistema mostra una sezione Eventi consigliati per te")
    public void sistemaMostraEventiConsigliati() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("SUCCESS"))
                .andExpect(jsonPath("$.recommendedEvents").isNotEmpty());
    }

    @Then("il sistema mostra entrambi gli eventi nella sezione Eventi consigliati per te")
    public void sistemaMostraEntrambiGliEventi() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("SUCCESS"))
                .andExpect(jsonPath("$.recommendedEvents.length()").value(2));
    }

    @And("gli eventi sono ordinati in base alla distanza dalla posizione di riferimento salvata")
    public void eventiOrdinatiPerDistanza() throws Exception {
        result.andExpect(jsonPath("$.recommendedEvents[0].title").value("Evento Vicino"))
                .andExpect(jsonPath("$.recommendedEvents[1].title").value("Evento Lontano"));
    }

    @And("evento {int} viene visualizzato prima di evento {int} nell'ordine finale")
    public void ordineSpecificoTraEventi(int primo, int secondo) throws Exception {
        result.andExpect(jsonPath("$.recommendedEvents[0].title").value("Evento " + primo))
                .andExpect(jsonPath("$.recommendedEvents[1].title").value("Evento " + secondo));
    }

    @Then("il sistema mostra il messaggio {string}")
    public void sistemaMostraMessaggioErrore(String messaggio) throws Exception {
        if (messaggio.equals("NO_LOCATION_MATCH")) {
            result.andExpect(jsonPath("$.messageCode").value("NO_LOCATION_MATCH"));
        } else {
            result.andExpect(jsonPath("$.messageCode").value("NO_TAG_MATCH"));

        }
    }

    private Event creaEventoNelDB(String titolo, String macroTag, String microTag, double lat, double lon) {
        Event event = new Event();
        event.setTitle(titolo);
        event.setDateTime(LocalDateTime.now().plusDays(5));
        event.setDuration(2.0);
        event.setLocation("Location di test");
        event.setLatitude(lat);
        event.setLongitude(lon);
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(creatorUser);
        event.setParticipants(new HashSet<>());

        List<EventTag> tags = new ArrayList<>();
        tags.add(new EventTag(macroTag, microTag));
        event.setTags(tags);

        return eventRepository.save(event);
    }
}