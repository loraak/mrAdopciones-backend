package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.Mascota;
import com.mradopciones.backend.repositories.MascotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MascotaService {
    private final MascotaRepository mascotaRepository;

    public MascotaService(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    public List<Mascota> getAllMascotas() {
        return mascotaRepository.findAll();
    }

    public List<Mascota> getMascotasByTipo(String tipo) {
        if (tipo == null || tipo.equalsIgnoreCase("todos")) {
            return mascotaRepository.findAll();
        }
        return mascotaRepository.findByTipoIgnoreCase(tipo);
    }

    public List<Mascota> getMascotasByDonante(Long donanteId) {
        return mascotaRepository.findByDonanteId(donanteId);
    }

    public Mascota getMascotaById(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada" + id));
    }

    public Mascota saveMascota(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    public Mascota updateMascota(Long id, Mascota mascotaDetails) {
        Mascota mascota = getMascotaById(id);
        mascota.setNombre(mascotaDetails.getNombre());
        mascota.setEdad(mascotaDetails.getEdad());
        mascota.setDescripcion(mascotaDetails.getDescripcion());
        mascota.setStatus(mascotaDetails.getStatus());
        if (mascotaDetails.getEtiquetas() != null) {
            mascota.setEtiquetas(mascotaDetails.getEtiquetas());
        }
        return mascotaRepository.save(mascota);
    }

    public void deleteMascota(Long id) {
        mascotaRepository.deleteById(id);
    }


}
