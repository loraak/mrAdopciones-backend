package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.LoginRequest;
import com.mradopciones.backend.entities.DTOs.LoginResponse;
import com.mradopciones.backend.entities.DTOs.RegistroRequest;
import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Tag("integration")
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1",
        "app.jwt.secret=clave-de-pruebas-con-mas-de-32-caracteres-123",
        "app.uploads-dir=target/test-uploads"
})
@Transactional
class AuthServiceIntegrationTest {

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JWTService jwtService;

    private RegistroRequest registro(String usuario, String correo) {
        return new RegistroRequest(usuario, correo, "Clave1234", Rol.ADOPTANTE, "4421234567", null, null, null);
    }

    private void assertEstatus(Throwable error, int esperado) {
        assertThat(error).isInstanceOfSatisfying(ResponseStatusException.class,
                ex -> assertThat(ex.getStatusCode().value()).isEqualTo(esperado));
    }

    @Test
    void registrar_guardaLaContrasenaCifradaEnLaBaseDeDatos() {
        usuarioService.registrar(registro("laura", "laura@mail.com"));

        Usuario guardado = usuarioRepository.findByUsernameIgnoreCase("laura").orElseThrow();

        assertThat(guardado.getContrasenia()).isNotEqualTo("Clave1234");
        assertThat(guardado.getContrasenia()).startsWith("$2");
        assertThat(passwordEncoder.matches("Clave1234", guardado.getContrasenia())).isTrue();
    }

    @Test
    void registrarYLogin_conUsuarioOCorreo_devuelveUnTokenValido() {
        usuarioService.registrar(registro("laura", "laura@mail.com"));
        Long idGuardado = usuarioRepository.findByUsernameIgnoreCase("laura").orElseThrow().getId();

        LoginResponse porUsuario = usuarioService.login(new LoginRequest("laura", "Clave1234"));
        LoginResponse porCorreo = usuarioService.login(new LoginRequest("LAURA@MAIL.COM", "Clave1234"));

        assertThat(jwtService.validar(porUsuario.token()).userId()).isEqualTo(idGuardado);
        assertThat(jwtService.validar(porCorreo.token()).userId()).isEqualTo(idGuardado);
    }

    @Test
    void login_conContrasenaIncorrecta_lanza401() {
        usuarioService.registrar(registro("laura", "laura@mail.com"));

        Throwable error = catchThrowable(() -> usuarioService.login(new LoginRequest("laura", "incorrecta1")));

        assertEstatus(error, 401);
    }

    @Test
    void registrarConUsuarioOCorreoRepetido_lanza409() {
        usuarioService.registrar(registro("laura", "laura@mail.com"));

        Throwable correoRepetido = catchThrowable(() ->
                usuarioService.registrar(registro("otra", "LAURA@MAIL.COM")));
        Throwable usuarioRepetido = catchThrowable(() ->
                usuarioService.registrar(registro("LAURA", "otro@mail.com")));

        assertEstatus(correoRepetido, 409);
        assertEstatus(usuarioRepetido, 409);
    }
}