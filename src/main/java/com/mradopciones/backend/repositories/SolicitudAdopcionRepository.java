package com.mradopciones.backend.repositories;

import com.mradopciones.backend.entities.SolicitudAdopcion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudAdopcionRepository extends JpaRepository<SolicitudAdopcion, Long> {
    @EntityGraph(attributePaths = {"mascota", "adoptante"})
    List<SolicitudAdopcion> findByMascotaDonanteIdOrderByIdDesc(Long donateId);

    @EntityGraph(attributePaths = {"mascota", "adoptante"})
    List<SolicitudAdopcion> findByAdoptanteIdOrderByIdDesc(Long adoptanteId);

    boolean existsByMascotaIdAndAdoptanteIdAndEstatus(Long mascotaId, Long adoptanteId, String estatus);
}
