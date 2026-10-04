package com.mradopciones.backend.entities.DTOs;

public record LoginResponse (String token, UsuarioResponse user) {}
