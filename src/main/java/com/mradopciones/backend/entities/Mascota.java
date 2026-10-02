package com.mradopciones.backend.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "mascotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer edad;

    @Column(nullable = false)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String imagenUrl;

    private String status;

    @ElementCollection
    @CollectionTable(name = "mascotas_etiquetas", joinColumns = @JoinColumn(name = "mascota_id"))
    @Column(name = "etiqueta")
    private List<String> etiquetas;

    @Column(name = "donante_id", nullable = false)
    private Long donanteId;


}

