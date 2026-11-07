package com.mradopciones.backend.requests;

import com.mradopciones.backend.entities.DTOs.LoginRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestValidationTest {

    private static Validator validador;

    @BeforeAll
    static void crearValidador() {
        validador = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> camposConError(LoginRequest solicitud) {
        return validador.validate(solicitud).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void datosValidos_noGeneranErrores() {
        assertThat(camposConError(new LoginRequest("maria", "Clave1234"))).isEmpty();
    }

    @Test
    void camposVacios_seRechazan() {
        assertThat(camposConError(new LoginRequest("", "   ")))
                .containsExactlyInAnyOrder("username", "password");
    }

    @Test
    void contrasenaDe72Caracteres_seAcepta_yDe73_seRechaza() {
        assertThat(camposConError(new LoginRequest("maria", "x".repeat(72)))).isEmpty();
        assertThat(camposConError(new LoginRequest("maria", "x".repeat(73)))).containsExactly("password");
    }

    @Test
    void usuarioDe100Caracteres_seAcepta_yDe101_seRechaza() {
        assertThat(camposConError(new LoginRequest("u".repeat(100), "Clave1234"))).isEmpty();
        assertThat(camposConError(new LoginRequest("u".repeat(101), "Clave1234"))).containsExactly("username");
    }
}