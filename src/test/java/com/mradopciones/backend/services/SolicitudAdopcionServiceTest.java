package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.SolicitudRequest;
import com.mradopciones.backend.entities.DTOs.SolicitudResponse;
import com.mradopciones.backend.entities.Mascota;
import com.mradopciones.backend.entities.SolicitudAdopcion;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.MascotaRepository;
import com.mradopciones.backend.repositories.SolicitudAdopcionRepository;
import com.mradopciones.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** AUT-03 | Unitarias | Reglas de solicitudes de adopción (CP-13, CP-18, CP-21). */
@ExtendWith(MockitoExtension.class)
class SolicitudAdopcionServiceTest {

    private static final long DONANTE_ID = 5L;
    private static final long ADOPTANTE_ID = 2L;

    @Mock private SolicitudAdopcionRepository solicitudRepository;
    @Mock private MascotaRepository mascotaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private SolicitudAdopcionService service;

    private Usuario adoptante;

    @BeforeEach
    void preparar() {
        adoptante = new Usuario();
        adoptante.setId(ADOPTANTE_ID);
        adoptante.setUsername("ana");
        adoptante.setCorreo("ana@mail.com");
    }

    private Mascota mascota(String estatus) {
        Mascota m = new Mascota();
        m.setId(10L);
        m.setNombre("Firulais");
        m.setDonanteId(DONANTE_ID);
        m.setEstatus(estatus);
        return m;
    }

    private SolicitudAdopcion solicitud(Mascota m, String estatus) {
        SolicitudAdopcion s = new SolicitudAdopcion();
        s.setId(1L);
        s.setMascota(m);
        s.setAdoptante(adoptante);
        s.setMensaje("Quiero adoptarlo");
        s.setEstatus(estatus);
        return s;
    }

    private void assertEstatus(Throwable error, int esperado) {
        assertThat(error).isInstanceOfSatisfying(ResponseStatusException.class,
                ex -> assertThat(ex.getStatusCode().value()).isEqualTo(esperado));
    }

    // ---------- cambiarEstatus ----------

    @Test
    void aceptar_porElDueno_pasaLaMascotaAEnProceso() {
        Mascota m = mascota("Disponible");
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud(m, "Pendiente")));

        SolicitudResponse r = service.cambiarEstatus(1L, DONANTE_ID, "Aceptada");

        assertThat(r.estatus()).isEqualTo("Aceptada");
        assertThat(m.getEstatus()).isEqualTo("En proceso");
    }

    @Test
    void rechazar_noCambiaElEstatusDeLaMascota() {
        Mascota m = mascota("Disponible");
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud(m, "Pendiente")));

        SolicitudResponse r = service.cambiarEstatus(1L, DONANTE_ID, "Rechazada");

        assertThat(r.estatus()).isEqualTo("Rechazada");
        assertThat(m.getEstatus()).isEqualTo("Disponible");
    }

    @Test
    void donanteAjeno_noPuedeResponder_403() {
        SolicitudAdopcion s = solicitud(mascota("Disponible"), "Pendiente");
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(s));

        Throwable e = catchThrowable(() -> service.cambiarEstatus(1L, 99L, "Aceptada"));

        assertEstatus(e, 403);
        assertThat(s.getEstatus()).isEqualTo("Pendiente");
    }

    @Test
    void solicitudYaRespondida_409() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud(mascota("En proceso"), "Aceptada")));

        assertEstatus(catchThrowable(() -> service.cambiarEstatus(1L, DONANTE_ID, "Rechazada")), 409);
    }

    @Test
    void solicitudInexistente_404() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.empty());

        assertEstatus(catchThrowable(() -> service.cambiarEstatus(1L, DONANTE_ID, "Aceptada")), 404);
    }

    /** Defecto D2 (CP-18): solo una solicitud por mascota puede aceptarse. */
    @Test
    void aceptarSegundaSolicitud_deUnaMascotaYaEnProceso_409() {
        Mascota yaEnProceso = mascota("En proceso");
        SolicitudAdopcion segunda = solicitud(yaEnProceso, "Pendiente");
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(segunda));

        Throwable e = catchThrowable(() -> service.cambiarEstatus(1L, DONANTE_ID, "Aceptada"));

        assertEstatus(e, 409);
        assertThat(segunda.getEstatus()).isEqualTo("Pendiente");
    }

    // ---------- crear ----------

    @Test
    void crear_valida_quedaPendienteConMensajeRecortado() {
        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota("Disponible")));
        when(usuarioRepository.findById(ADOPTANTE_ID)).thenReturn(Optional.of(adoptante));
        when(solicitudRepository.save(any(SolicitudAdopcion.class))).thenAnswer(i -> i.getArgument(0));

        SolicitudResponse r = service.crear(ADOPTANTE_ID, new SolicitudRequest(10L, "  Quiero adoptarlo  "));

        assertThat(r.estatus()).isEqualTo("Pendiente");
        assertThat(r.mensaje()).isEqualTo("Quiero adoptarlo");
        assertThat(r.solicitanteNombre()).isEqualTo("ana");
    }

    @Test
    void crear_mascotaNoDisponible_409() {
        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota("Adoptado")));

        assertEstatus(catchThrowable(() -> service.crear(ADOPTANTE_ID, new SolicitudRequest(10L, "Quiero adoptarlo"))), 409);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void crear_conSolicitudPendienteDuplicada_409() {
        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota("Disponible")));
        when(solicitudRepository.existsByMascotaIdAndAdoptanteIdAndEstatus(10L, ADOPTANTE_ID, "Pendiente")).thenReturn(true);

        assertEstatus(catchThrowable(() -> service.crear(ADOPTANTE_ID, new SolicitudRequest(10L, "Quiero adoptarlo"))), 409);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void crear_mascotaInexistente_404() {
        when(mascotaRepository.findById(10L)).thenReturn(Optional.empty());

        assertEstatus(catchThrowable(() -> service.crear(ADOPTANTE_ID, new SolicitudRequest(10L, "Quiero adoptarlo"))), 404);
    }
}