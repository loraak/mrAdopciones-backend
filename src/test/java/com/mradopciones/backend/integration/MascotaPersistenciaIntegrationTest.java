package com.mradopciones.backend.integration;

import com.mradopciones.backend.entities.DTOs.MascotaRequest;
import com.mradopciones.backend.entities.DTOs.MascotaResponse;
import com.mradopciones.backend.entities.Mascota;
import com.mradopciones.backend.repositories.MascotaRepository;
import com.mradopciones.backend.services.MascotaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/** AUT-06 | Persistencia | MascotaService + JPA + H2: lo que se guarda es lo que se lee (CP-11, CP-17). */
class MascotaPersistenciaIntegrationTest extends ApiIntegrationBase {

    @Autowired private MascotaService mascotaService;
    @Autowired private MascotaRepository mascotaRepository;
    @PersistenceContext private EntityManager em;

    private MascotaRequest req(String nombre, String tipo, String estatus, List<String> etiquetas) {
        return new MascotaRequest(nombre, 2, tipo, "Descripcion", null, estatus, etiquetas);
    }

    /** Fuerza el viaje real a la base: escribe y vacía el caché para que la lectura venga de H2. */
    private void sincronizar() {
        em.flush();
        em.clear();
    }

    @Test
    void crear_guardaDatosNormalizados_yEstatusPorDefecto() {
        MascotaResponse r = mascotaService.crear(7L, new MascotaRequest(
                "  Firulais ", 3, " PERROS ", " Muy jugueton ", "  ", null,
                List.of("sano", " sano ", "jugueton", "   ")));
        sincronizar();

        Mascota guardada = mascotaRepository.findById(r.id()).orElseThrow();
        assertThat(guardada.getNombre()).isEqualTo("Firulais");
        assertThat(guardada.getTipo()).isEqualTo("perros");
        assertThat(guardada.getDescripcion()).isEqualTo("Muy jugueton");
        assertThat(guardada.getImagen()).isNull();
        assertThat(guardada.getEstatus()).isEqualTo("Disponible");
        assertThat(guardada.getDonanteId()).isEqualTo(7L);
        assertThat(guardada.getEtiquetas()).containsExactlyInAnyOrder("sano", "jugueton");
    }

    @Test
    void actualizar_porElDueno_persisteLosCambios_yPorOtro_403SinTocarDatos() {
        Long id = mascotaService.crear(7L, req("Firulais", "perros", null, List.of("sano"))).id();
        sincronizar();

        Throwable e = catchThrowable(() -> mascotaService.actualizar(id, 8L, req("Hackeado", "gatos", "Adoptado", List.of())));
        assertThat(e).isInstanceOfSatisfying(ResponseStatusException.class,
                ex -> assertThat(ex.getStatusCode().value()).isEqualTo(403));
        sincronizar();
        assertThat(mascotaRepository.findById(id).orElseThrow().getNombre()).isEqualTo("Firulais");

        mascotaService.actualizar(id, 7L, req("Rex", "gatos", "Adoptado", List.of("tranquilo")));
        sincronizar();
        Mascota actualizada = mascotaRepository.findById(id).orElseThrow();
        assertThat(actualizada.getNombre()).isEqualTo("Rex");
        assertThat(actualizada.getTipo()).isEqualTo("gatos");
        assertThat(actualizada.getEstatus()).isEqualTo("Adoptado");
        assertThat(actualizada.getEtiquetas()).containsExactly("tranquilo");
    }

    @Test
    void eliminar_borraElRegistro() {
        Long id = mascotaService.crear(7L, req("Firulais", "perros", null, List.of("sano"))).id();
        sincronizar();

        mascotaService.eliminar(id, 7L);
        sincronizar();

        assertThat(mascotaRepository.findById(id)).isEmpty();
    }

    @Test
    void catalogo_soloDisponibles_masRecientesPrimero_yFiltraPorTipo() {
        mascotaService.crear(7L, req("A", "perros", null, List.of()));
        mascotaService.crear(7L, req("B", "perros", "En proceso", List.of()));
        mascotaService.crear(7L, req("C", "gatos", null, List.of()));
        mascotaService.crear(7L, req("D", "perros", null, List.of()));
        sincronizar();

        assertThat(mascotaService.getCatalogo("todos")).extracting(MascotaResponse::nombre)
                .containsExactly("D", "C", "A");
        assertThat(mascotaService.getCatalogo("perros")).extracting(MascotaResponse::nombre)
                .containsExactly("D", "A");
    }

    @Test
    void misMascotas_incluyeTodosLosEstatus_peroSoloLasPropias() {
        mascotaService.crear(7L, req("Mia1", "perros", null, List.of()));
        mascotaService.crear(7L, req("Mia2", "gatos", "Adoptado", List.of()));
        mascotaService.crear(8L, req("Ajena", "otros", null, List.of()));
        sincronizar();

        assertThat(mascotaService.getMisMascotas(7L)).extracting(MascotaResponse::nombre)
                .containsExactly("Mia2", "Mia1");
    }
}