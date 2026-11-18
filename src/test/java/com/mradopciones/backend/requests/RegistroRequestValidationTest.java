package com.mradopciones.backend.requests;

import com.mradopciones.backend.entities.DTOs.RegistroRequest;
import com.mradopciones.backend.entities.Rol;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RegistroRequestValidationTest {

    private static Validator validador;

    @BeforeAll
    static void crearValidador() {
        validador = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private RegistroRequest req(String usuario, String correo, String clave, Rol rol) {
        return new RegistroRequest(usuario, correo, clave, rol, "4421234567", "Estudiante", null, null);
    }

    private Set<String> camposConError(RegistroRequest r) {
        return validador.validate(r).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void datosValidos_noGeneranErrores() {
        assertThat(camposConError(req("ana", "ana@mail.com", "Clave1234", Rol.ADOPTANTE))).isEmpty();
    }

    @Test
    void camposNulos_seRechazan() {
        RegistroRequest r = new RegistroRequest(null, null, null, null, null, null, null, null);
        assertThat(camposConError(r)).containsExactlyInAnyOrder("username", "correo", "contrasena", "rol");
    }

    @Test
    void camposVacios_seRechazan() {
        assertThat(camposConError(req("", "", "   ", Rol.DONANTE)))
                .containsExactlyInAnyOrder("username", "correo", "contrasena");
    }

    @Test
    void usuarioDe2_seRechaza_yDe3_seAcepta() {
        assertThat(camposConError(req("ab", "ana@mail.com", "Clave1234", Rol.ADOPTANTE))).containsExactly("username");
        assertThat(camposConError(req("abc", "ana@mail.com", "Clave1234", Rol.ADOPTANTE))).isEmpty();
    }

    /** Defecto D1: la especificación (Práctica 8, CP-02/CP-03) dice 3 a 30; el código decía max = 15. */
    @Test
    void usuarioDe30_seAcepta_yDe31_seRechaza() {
        assertThat(camposConError(req("u".repeat(30), "ana@mail.com", "Clave1234", Rol.ADOPTANTE))).isEmpty();
        assertThat(camposConError(req("u".repeat(31), "ana@mail.com", "Clave1234", Rol.ADOPTANTE)))
                .containsExactly("username");
    }

    /** Defecto D1 (parte 2): el mensaje al usuario debe coincidir con el límite real. */
    @Test
    void mensajeDeUsuarioLargo_mencionaElLimiteCorrecto() {
        var mensajes = validador.validate(req("u".repeat(31), "ana@mail.com", "Clave1234", Rol.ADOPTANTE))
                .stream().map(v -> v.getMessage()).toList();
        assertThat(mensajes).containsExactly("El usuario debe tener entre 3 y 30 caracteres");
    }

    @Test
    void contrasenaLimites_7y73SeRechazan_8y72SeAceptan() {
        assertThat(camposConError(req("ana", "ana@mail.com", "x".repeat(7), Rol.ADOPTANTE))).containsExactly("contrasena");
        assertThat(camposConError(req("ana", "ana@mail.com", "x".repeat(8), Rol.ADOPTANTE))).isEmpty();
        assertThat(camposConError(req("ana", "ana@mail.com", "x".repeat(72), Rol.ADOPTANTE))).isEmpty();
        assertThat(camposConError(req("ana", "ana@mail.com", "x".repeat(73), Rol.ADOPTANTE))).containsExactly("contrasena");
    }

    @Test
    void correoSinFormatoValido_seRechaza() {
        for (String malo : new String[]{"sin-arroba", "ana@", "ana mail@x.com"}) {
            assertThat(camposConError(req("ana", malo, "Clave1234", Rol.ADOPTANTE)))
                    .as("correo: " + malo).containsExactly("correo");
        }
    }
}