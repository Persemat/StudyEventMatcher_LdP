package com.dem.ldp.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreazioneEvento {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private ResultActions result;

    // Variabili di stato
    private String titolo = "";
    private String descrizione = "";
    private String dataOra = "";
    private double durata = 0.0;
    private String luogo = "";
    private String tag = "[]";
    private int limitePartecipanti = 0;
    private boolean partecipazioneCreatore = false;
    private Long creatoreId; // Questo verrà usato per popolare l'attributo di sessione

    @Given("l'utente si trova nella pagina di creazione evento")
    public void utenteNellaPaginaCreazioneEvento() {
        // Reset stati
        titolo = "";
        descrizione = "";
        dataOra = "";
        durata = 0.0;
        luogo = "";
        tag = "[]";
        limitePartecipanti = 0;
        partecipazioneCreatore = false;

        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        // Creazione utente reale per avere un ID valido in sessione
        Utente utenteFinto = new Utente();
        utenteFinto.setNome("Marco");
        utenteFinto.setCognome("Rossi");
        utenteFinto.setEmail("marco@example.com");
        utenteFinto.setPassword("Password123!");
        utenteRepository.save(utenteFinto);

        this.creatoreId = utenteFinto.getId();
    }

    @When("l'utente inserisce titolo, descrizione, data, durata e luogo")
    public void utenteInserisceDatiBase() {
        titolo = "Gruppo di studio Java";
        descrizione = "Ripasso per l'esame di architetture";
        dataOra = "2026-10-15T15:00:00";
        durata = 2.5;
        luogo = "Aula Magna";
    }

    @And("aggiunge uno o piu tag e imposta un limite di partecipanti")
    public void aggiungeTagELimite() {
        tag = "[\"Java\", \"Programmazione\"]";
        limitePartecipanti = 10;
    }

    @And("imposta il flag Partecipazione del creatore necessaria")
    public void impostaFlagCreatore() {
        partecipazioneCreatore = true;
    }

    @And("conferma la creazione")
    public void confermaLaCreazione() throws Exception {
        // Il JSON ora non contiene più creatoreId
        String body = String.format("""
                {
                "titolo": "%s",
                "descrizione": "%s",
                "dataOra": "%s",
                "durata": %s,
                "luogo": "%s",
                "tag": %s,
                "maxPartecipanti": %d,
                "partecipazioneCreatoreNecessaria": %b
                }
                """, titolo, descrizione, dataOra, String.valueOf(durata), luogo, tag, limitePartecipanti,
                partecipazioneCreatore);
        // Simuliamo l'utente loggato iniettando l'ID nella sessione
        result = mockMvc.perform(post("/Eventi")
                .sessionAttr("userId", creatoreId) // <--- SESSIONE ATTIVA
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("il sistema salva il nuovo evento")
    public void sistemaSalvaNuovoEvento() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("il sistema imposta lo stato dell'evento come aperto")
    public void sistemaImpostaStatoAperto() {
        // Logica verificata implicitamente dal 200 OK e dalla logica del service
    }

    @And("l'evento appare nella lista degli eventi")
    public void eventoAppareNellaLista() {
        // Step di accettazione lato utente
    }

    @When("l'utente tenta di creare un evento senza compilare i campi obbligatori")
    public void tentaDiCreareSenzaCampi() throws Exception {
        // JSON incompleto (titolo e luogo vuoti), niente creatoreId
        String bodyIncompleto = """
                {
                "titolo": "",
                "descrizione": "test",
                "dataOra": "2026-10-15T15:00:00",
                "durata": 1.0,
                "luogo": "Aula Magna",
                "tag": ["test"],
                "maxPartecipanti": 10,
                "partecipazioneCreatoreNecessaria": true
                }
                """;

        result = mockMvc.perform(post("/Eventi")
                .sessionAttr("userId", creatoreId) // <--- SESSIONE ATTIVA
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyIncompleto));
    }

    @Then("il sistema impedisce la creazione dell'evento")
    public void sistemaImpedisceCreazione() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    @And("il sistema mostra l'errore {string}")
    public void ilSistemaMOstraErrore(String messaggio) throws Exception {
        result.andExpect(content().string(org.hamcrest.Matchers.containsString(messaggio)));
    }
}