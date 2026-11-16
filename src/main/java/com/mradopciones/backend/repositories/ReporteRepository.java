package com.mradopciones.backend.repositories;

import com.mradopciones.backend.entities.Reporte;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    @EntityGraph(attributePaths = {"solicitud", "solicitud.mascota", "reportante", "reportado"})
    List<Reporte> findAllByOrderByIdDesc();
}