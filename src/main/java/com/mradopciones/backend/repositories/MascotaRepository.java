package com.mradopciones.backend.repositories;

import com.mradopciones.backend.entities.Mascota;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    @EntityGraph(attributePaths = "etiquetas")
    List<Mascota> findByEstatusIgnoreCaseOrderByIdDesc(String status);

    @EntityGraph(attributePaths = "etiquetas")
    List<Mascota> findByEstatusIgnoreCaseAndTipoIgnoreCaseOrderByIdDesc(String status, String tipo);

    @EntityGraph(attributePaths = "etiquetas")
    List<Mascota> findByDonanteIdOrderByIdDesc(Long donanteId);
}