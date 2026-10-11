package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.SolicitudRequest;
import com.mradopciones.backend.entities.DTOs.SolicitudResponse;
import com.mradopciones.backend.entities.Mascota;
import com.mradopciones.backend.entities.SolicitudAdopcion;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.MascotaRepository;
import com.mradopciones.backend.repositories.SolicitudAdopcionRepository;
import com.mradopciones.backend.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SolicitudAdopcionService {

    public static final String PENDIENTE = "Pendiente";
    public static final String ACEPTADA = "Aceptada";
    public static final String RECHAZADA = "Rechazada";

    private static final String MASCOTA_DISPONIBLE = "Disponible";
    private static final String MASCOTA_EN_PROCESO = "En proceso";

    private final SolicitudAdopcionRepository solicitudRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    /** Un adoptante pide adoptar una mascota. */
    @Transactional
    public SolicitudResponse crear(Long adoptanteId, SolicitudRequest req) {
        Mascota mascota = mascotaRepository.findById(req.mascotaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        if (!MASCOTA_DISPONIBLE.equalsIgnoreCase(mascota.getEstatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta mascota ya no está disponible");
        }
        if (solicitudRepository.existsByMascotaIdAndAdoptanteIdAndEstatus(mascota.getId(), adoptanteId, PENDIENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una solicitud pendiente para esta mascota");
        }

        Usuario adoptante = usuarioRepository.findById(adoptanteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        SolicitudAdopcion solicitud = new SolicitudAdopcion();
        solicitud.setMascota(mascota);
        solicitud.setAdoptante(adoptante);
        solicitud.setMensaje(req.mensaje().trim());
        solicitud.setEstatus(PENDIENTE);

        return SolicitudResponse.from(solicitudRepository.save(solicitud));
    }

    /** Solicitudes que llegaron a las mascotas de este donante. */
    @Transactional
    public List<SolicitudResponse> recibidas(Long donanteId) {
        return solicitudRepository.findByMascotaDonanteIdOrderByIdDesc(donanteId).stream()
                .map(SolicitudResponse::from)
                .toList();
    }

    /** Solicitudes que envió este adoptante. */
    @Transactional
    public List<SolicitudResponse> enviadas(Long adoptanteId) {
        return solicitudRepository.findByAdoptanteIdOrderByIdDesc(adoptanteId).stream()
                .map(SolicitudResponse::from)
                .toList();
    }

    /** El donante acepta o rechaza una solicitud de una de sus mascotas. */
    @Transactional
    public SolicitudResponse cambiarEstatus(Long solicitudId, Long donanteId, String nuevoEstatus) {
        SolicitudAdopcion solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        Mascota mascota = solicitud.getMascota();

        // Solo el dueño de la mascota puede responder
        if (!Objects.equals(mascota.getDonanteId(), donanteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta solicitud no es de tus mascotas");
        }
        if (!PENDIENTE.equals(solicitud.getEstatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta solicitud ya fue respondida");
        }

        solicitud.setEstatus(nuevoEstatus);

        // Al aceptar, la mascota sale del catálogo. El donante puede cambiarlo después en "Mis mascotas".
        if (ACEPTADA.equals(nuevoEstatus)) {
            mascota.setEstatus(MASCOTA_EN_PROCESO);
        }

        return SolicitudResponse.from(solicitud);
    }
}