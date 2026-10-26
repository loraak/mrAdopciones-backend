package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.ChatInfoResponse;
import com.mradopciones.backend.entities.DTOs.MensajeResponse;
import com.mradopciones.backend.entities.Mascota;
import com.mradopciones.backend.entities.Mensaje;
import com.mradopciones.backend.entities.SolicitudAdopcion;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.MensajeRepository;
import com.mradopciones.backend.repositories.SolicitudAdopcionRepository;
import com.mradopciones.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final SolicitudAdopcionRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public ChatInfoResponse info(Long solicitudId, Long userId) {
        SolicitudAdopcion solicitud = chatHabilitado(solicitudId, userId);
        Mascota mascota = solicitud.getMascota();
        Usuario adoptante = solicitud.getAdoptante();
        Usuario donante = usuarioRepository.findById(mascota.getDonanteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));

        boolean soyAdoptante = esAdoptante(solicitud, userId);
        return new ChatInfoResponse(
                solicitud.getId(),
                mascota.getId(),
                mascota.getNombre(),
                soyAdoptante ? adoptante.getUsername() : donante.getUsername(),
                soyAdoptante ? donante.getUsername() : adoptante.getUsername()
        );
    }

    /** Mensajes posteriores a "despuesDe", sin incluir los que el usuario ya limpió. */
    @Transactional(readOnly = true)
    public List<MensajeResponse> listar(Long solicitudId, Long userId, long despuesDe) {
        SolicitudAdopcion solicitud = chatHabilitado(solicitudId, userId);
        long limpioHasta = limpioHasta(solicitud, userId);
        long desde = Math.max(despuesDe, limpioHasta);

        return mensajeRepository.findBySolicitudIdAndIdGreaterThanOrderByIdAsc(solicitudId, desde).stream()
                .map(m -> MensajeResponse.from(m, userId))
                .toList();
    }

    @Transactional
    public MensajeResponse enviar(Long solicitudId, Long userId, String contenido) {
        SolicitudAdopcion solicitud = chatHabilitado(solicitudId, userId);

        // El emisor siempre sale del token, nunca del cuerpo de la petición
        Usuario emisor = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        Mensaje mensaje = new Mensaje();
        mensaje.setSolicitud(solicitud);
        mensaje.setEmisor(emisor);
        mensaje.setContenido(contenido.trim());

        return MensajeResponse.from(mensajeRepository.save(mensaje), userId);
    }

    /** Oculta los mensajes actuales solo para este usuario. No borra nada. */
    @Transactional
    public void limpiarHistorial(Long solicitudId, Long userId) {
        SolicitudAdopcion solicitud = chatHabilitado(solicitudId, userId);
        Long ultimo = mensajeRepository.ultimoId(solicitudId);

        if (esAdoptante(solicitud, userId)) {
            solicitud.setChatLimpioAdoptante(ultimo);
        } else {
            solicitud.setChatLimpioDonante(ultimo);
        }
    }

    // ---------- reglas de acceso ----------

    private SolicitudAdopcion chatHabilitado(Long solicitudId, Long userId) {
        SolicitudAdopcion solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));

        // A quien no participa se le responde igual que si no existiera: no se revela que hay un chat
        if (!esAdoptante(solicitud, userId) && !esDonante(solicitud, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada");
        }
        if (!SolicitudAdopcionService.ACEPTADA.equals(solicitud.getEstatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El chat se habilita cuando la solicitud es aceptada");
        }
        return solicitud;
    }

    private boolean esAdoptante(SolicitudAdopcion solicitud, Long userId) {
        return Objects.equals(solicitud.getAdoptante().getId(), userId);
    }

    private boolean esDonante(SolicitudAdopcion solicitud, Long userId) {
        return Objects.equals(solicitud.getMascota().getDonanteId(), userId);
    }

    private long limpioHasta(SolicitudAdopcion solicitud, Long userId) {
        Long valor = esAdoptante(solicitud, userId)
                ? solicitud.getChatLimpioAdoptante()
                : solicitud.getChatLimpioDonante();
        return valor == null ? 0L : valor;
    }
}