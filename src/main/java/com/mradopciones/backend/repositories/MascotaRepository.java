package com.mradopciones.backend.repositories;

import com.mradopciones.backend.entities.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByTipoIgnoreCase(String tipo);

    List<Mascota> findByDonanteId(Long donanteId);
}
