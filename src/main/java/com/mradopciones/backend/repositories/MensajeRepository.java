package com.mradopciones.backend.repositories;

import com.mradopciones.backend.entities.Mensaje;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @EntityGraph(attributePaths = "emisor")
    List<Mensaje> findBySolicitudIdAndIdGreaterThanOrderByIdAsc(Long solicitudId, Long id);

    @Query("select coalesce(max(m.id), 0L) from Mensaje m where m.solicitud.id = :solicitudId")
    Long ultimoId(@Param("solicitudId") Long solicitudId);
}