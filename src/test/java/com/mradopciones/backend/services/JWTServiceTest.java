package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.entities.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JWTServiceTest {

    private static final String SECRETO = "clave-de-pruebas-con-mas-de-32-caracteres-123";
    private static final String OTRO_SECRETO = "otra-clave-distinta-con-mas-de-32-caracteres";

    private Usuario donante() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setUsername("laura");
        u.setRol(Rol.DONANTE);
        return u;
    }

    private void assertSesionInvalida(Runnable accion) {
        assertThatThrownBy(accion::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(401));
    }

    @Test
    void tokenValido_devuelveLosDatosDelUsuario() {
        JWTService servicio = new JWTService(SECRETO, 60_000);

        String token = servicio.generarToken(donante());
        var datos = servicio.validar(token);

        assertThat(datos.userId()).isEqualTo(7L);
        assertThat(datos.username()).isEqualTo("laura");
        assertThat(datos.rol()).isEqualTo(Rol.DONANTE);
    }

    @Test
    void tokenYaExpirado_lanza401() {
        JWTService servicio = new JWTService(SECRETO, -1_000);
        String token = servicio.generarToken(donante());

        assertSesionInvalida(() -> servicio.validar(token));
    }

    @Test
    void secretoDe31Caracteres_noSeAcepta() {
        assertThatThrownBy(() -> new JWTService("x".repeat(31), 60_000))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secretoDe32Caracteres_seAcepta() {
        assertThatCode(() -> new JWTService("x".repeat(32), 60_000)).doesNotThrowAnyException();
    }

    // ---------- Casos INVALIDOS / ERROR ----------

    @Test
    void tokenManipulado_lanza401() {
        JWTService servicio = new JWTService(SECRETO, 60_000);
        String token = servicio.generarToken(donante());
        String alterado = token.substring(0, token.length() - 10) + "AAAAAAAAAA";

        assertSesionInvalida(() -> servicio.validar(alterado));
    }

    @Test
    void tokenFirmadoConOtraClave_lanza401() {
        String tokenAjeno = new JWTService(OTRO_SECRETO, 60_000).generarToken(donante());

        assertSesionInvalida(() -> new JWTService(SECRETO, 60_000).validar(tokenAjeno));
    }

    @Test
    void textoQueNoEsUnToken_lanza401() {
        assertSesionInvalida(() -> new JWTService(SECRETO, 60_000).validar("esto-no-es-un-token"));
    }
}