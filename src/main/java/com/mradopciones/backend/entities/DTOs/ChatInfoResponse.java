package com.mradopciones.backend.entities.DTOs;

public record ChatInfoResponse(
        Long solicitudId,
        Long mascotaId,
        String mascotaNombre,
        String miNombre,
        String otroNombre
) {}