package com.mradopciones.backend.controllers;

import com.mradopciones.backend.entities.Mascota;
import com.mradopciones.backend.services.MascotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "*")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService)  {
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> getMascotas(@RequestParam(required = false) String tipo) {
        List<Mascota> mascotas = mascotaService.getMascotasByTipo(tipo);
        return ResponseEntity.ok(mascotas);
    }

    @GetMapping("/donante/{donanteId}")
    public ResponseEntity<List<Mascota>> getMascotasByDonante(@PathVariable Long donanteId) {
        List<Mascota> mascotas = mascotaService.getMascotasByDonante(donanteId);
        return ResponseEntity.ok(mascotas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> getMascotaById(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.getMascotaById(id));
    }

    @PostMapping
    public ResponseEntity<Mascota> createMascota(@RequestBody Mascota mascota) {
        Mascota savedMascota = mascotaService.saveMascota(mascota);
        return new ResponseEntity<>(savedMascota, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> updateMascota(@PathVariable Long id, @RequestBody Mascota mascota) {
        Mascota updatedMascota = mascotaService.updateMascota(id, mascota);
        return ResponseEntity.ok(updatedMascota);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable Long id) {
        mascotaService.deleteMascota(id);
        return ResponseEntity.noContent().build();
    }
}
