package com.mradopciones.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Sin @Data a propósito: toString/equals sobre relaciones LAZY pueden disparar consultas inesperadas
@Entity
@Table(name = "solicitudes_adopcion")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudAdopcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adoptante_id", nullable = false)
    private Usuario adoptante;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    // Pendiente | Aceptada | Rechazada
    @Column(nullable = false)
    private String estatus;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    // "Limpiar historial": cada participante oculta los mensajes con id <= este valor.
    // Los mensajes no se borran: el otro participante los sigue viendo.
    @Column(name = "chat_limpio_adoptante")
    private Long chatLimpioAdoptante;

    @Column(name = "chat_limpio_donante")
    private Long chatLimpioDonante;

    @PrePersist
    void antesDeGuardar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}