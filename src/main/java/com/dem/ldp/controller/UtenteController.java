package com.dem.ldp.controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;


@RestController
@RequestMapping("/Utentes")
public class UtenteController {

    private final UtenteRepository UtenteRepository;

    public UtenteController(UtenteRepository UtenteRepository) {
        this.UtenteRepository = UtenteRepository;
    }

    @PostMapping
    public Utente createUtente(@RequestBody Utente Utente) {
        return UtenteRepository.save(Utente);
    }

    @GetMapping
    public List<Utente> getAllUtentes() {
        return UtenteRepository.findAll();
    }
}