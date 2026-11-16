package com.mradopciones.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
@Getter
@Setter
@NoArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudAdopcion solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reportante_id", nullable = false)
    private Usuario reportante;

    // Solo si el tipo es USUARIO; en reportes técnicos queda null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_id")
    private Usuario reportado;

    @Column(nullable = false)
    private String tipo;          // TECNICO | USUARIO

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private String estatus;       // Pendiente | Revisado

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }
}