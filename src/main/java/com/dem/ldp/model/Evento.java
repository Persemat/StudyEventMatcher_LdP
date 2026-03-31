package com.dem.ldp.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "eventi")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    //Campi Obbligatori
    @Column(nullable = false)
    private String titolo;

    @Column(nullable = false)
    private LocalDateTime dataOra;

    @Column(nullable = false)
    private String luogo;

    //ElementCOlletion per salvare una lista di tag
    @ElementCollection
    @CollectionTable(name = "evento_tag", joinColumns = @JoinColumn(name = "evento_id"))
    @Column(name = "tag", nullable = false)
    private List<String> tag;

    //campi non obbligatori
    @Column(length = 1000)
    private String descrizione;

    private Integer maxPartecipanti;

    private boolean partecipazioneCreatoreNecessaria;

    @Column(nullable = false)
    private String stato = "aperto";

    @ManyToOne(optional = false)
    @JoinColumn(name = "creatore_id", nullable = false)
    private Utente creatore;

    //costruttore vuoto
    public Evento() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Integer getMaxPartecipanti() {
        return maxPartecipanti;
    }

    public void setMaxPartecipanti(Integer maxPartecipanti) {
        this.maxPartecipanti = maxPartecipanti;
    }

    public boolean isPartecipazioneCreatoreNecessaria() {
        return partecipazioneCreatoreNecessaria;
    }

    public void setPartecipazioneCreatoreNecessaria(boolean partecipazioneCreatoreNecessaria) {
        this.partecipazioneCreatoreNecessaria = partecipazioneCreatoreNecessaria;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public Utente getCreatore() {
        return creatore;
    }

    public void setCreatore(Utente creatore) {
        this.creatore = creatore;
    }

    


}
