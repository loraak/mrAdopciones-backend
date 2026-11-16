package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.ReporteRequest;
import com.mradopciones.backend.entities.DTOs.ReporteResponse;
import com.mradopciones.backend.entities.Reporte;
import com.mradopciones.backend.entities.SolicitudAdopcion;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.ReporteRepository;
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
public class ReporteService {

    public static final String PENDIENTE = "Pendiente";
    public static final String REVISADO = "Revisado";

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MensajeService mensajeService;

    @Transactional
    public ReporteResponse crear(Long solicitudId, Long userId, ReporteRequest req) {
        // Valida que participe en el chat y que la solicitud esté aceptada
        SolicitudAdopcion solicitud = mensajeService.chatHabilitado(solicitudId, userId);

        Usuario reportante = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        Usuario reportado = null;
        if ("USUARIO".equals(req.tipo())) {
            Long otroId = Objects.equals(solicitud.getAdoptante().getId(), userId)
                    ? solicitud.getMascota().getDonanteId()
                    : solicitud.getAdoptante().getId();
            reportado = usuarioRepository.findById(otroId).orElse(null);
        }

        Reporte reporte = new Reporte();
        reporte.setSolicitud(solicitud);
        reporte.setReportante(reportante);   // siempre sale del token
        reporte.setReportado(reportado);
        reporte.setTipo(req.tipo());
        reporte.setDescripcion(req.descripcion().trim());
        reporte.setEstatus(PENDIENTE);

        return ReporteResponse.from(reporteRepository.save(reporte));
    }

    @Transactional
    public List<ReporteResponse> listar() {
        return reporteRepository.findAllByOrderByIdDesc().stream()
                .map(ReporteResponse::from)
                .toList();
    }

    @Transactional
    public ReporteResponse marcarRevisado(Long id) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
        reporte.setEstatus(REVISADO);
        return ReporteResponse.from(reporte);
    }
}