package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.MascotaRequest;
import com.mradopciones.backend.entities.DTOs.MascotaResponse;
import com.mradopciones.backend.entities.Mascota;
import com.mradopciones.backend.repositories.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MascotaService {

    public static final String DISPONIBLE = "Disponible";

    private final MascotaRepository mascotaRepository;

    @Transactional(readOnly = true)
    public List<MascotaResponse> getCatalogo(String tipo) {
        boolean todos = tipo == null || tipo.isBlank() || tipo.equalsIgnoreCase("todos");

        List<Mascota> mascotas = todos
                ? mascotaRepository.findByEstatusIgnoreCaseOrderByIdDesc(DISPONIBLE)
                : mascotaRepository.findByEstatusIgnoreCaseAndTipoIgnoreCaseOrderByIdDesc(DISPONIBLE, tipo.trim());

        return mascotas.stream().map(MascotaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MascotaResponse getMascotaById(Long id) {
        return MascotaResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<MascotaResponse> getMisMascotas(Long donanteId) {
        return mascotaRepository.findByDonanteIdOrderByIdDesc(donanteId).stream()
                .map(MascotaResponse::from)
                .toList();
    }

    @Transactional
    public MascotaResponse crear(Long donanteId, MascotaRequest req) {
        Mascota mascota = new Mascota();
        aplicar(mascota, req);
        mascota.setDonanteId(donanteId);
        if (mascota.getEstatus() == null) {
            mascota.setEstatus(DISPONIBLE);
        }
        return MascotaResponse.from(mascotaRepository.save(mascota));
    }

    @Transactional
    public MascotaResponse actualizar(Long id, Long donanteId, MascotaRequest req) {
        Mascota mascota = buscarDeDonante(id, donanteId);
        aplicar(mascota, req);
        return MascotaResponse.from(mascotaRepository.save(mascota));
    }

    @Transactional
    public void eliminar(Long id, Long donanteId) {
        mascotaRepository.delete(buscarDeDonante(id, donanteId));
    }

    private Mascota buscar(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mascota no encontrada con id " + id));
    }

    private Mascota buscarDeDonante(Long id, Long donanteId) {
        Mascota mascota = buscar(id);
        if (!Objects.equals(mascota.getDonanteId(), donanteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta mascota no es tuya");
        }
        return mascota;
    }

    private void aplicar(Mascota mascota, MascotaRequest req) {
        mascota.setNombre(req.nombre().trim());
        mascota.setEdad(req.edad());
        mascota.setTipo(req.tipo().trim().toLowerCase());
        mascota.setDescripcion(req.descripcion().trim());
        mascota.setImagen(req.imagen() == null || req.imagen().isBlank() ? null : req.imagen().trim());

        if (req.estatus() != null) {
            mascota.setEstatus(req.estatus());
        }

        List<String> etiquetas = req.etiquetas() == null ? List.of() : req.etiquetas().stream()
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
        mascota.setEtiquetas(new ArrayList<>(etiquetas));
    }
}