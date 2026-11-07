package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.LoginRequest;
import com.mradopciones.backend.entities.DTOs.LoginResponse;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas UNITARIAS de la logica de inicio de sesion.
 * El repositorio, el cifrador de contrasenas y el servicio de tokens son dobles de prueba (mocks):
 * aqui solo se evalua la regla de negocio de UsuarioService.login.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceLoginTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JWTService jwtService;   // si tu clase se llama JwtService, cambia el nombre

    @InjectMocks private UsuarioService usuarioService;

    private Usuario maria;

    @BeforeEach
    void preparar() {
        maria = new Usuario();
        maria.setId(1L);
        maria.setUsername("maria");
        maria.setCorreo("maria@mail.com");
        maria.setContrasenia("hash-bcrypt");
    }

    @Test
    void login_conCredencialesValidas_devuelveTokenYDatosDelUsuario() {
        when(usuarioRepository.findByUsernameIgnoreCase("maria")).thenReturn(Optional.of(maria));
        when(passwordEncoder.matches("Clave1234", "hash-bcrypt")).thenReturn(true);
        when(jwtService.generarToken(maria)).thenReturn("token-falso");

        LoginResponse respuesta = usuarioService.login(new LoginRequest("maria", "Clave1234"));

        assertThat(respuesta.token()).isEqualTo("token-falso");
        assertThat(respuesta.user().username()).isEqualTo("maria");
    }

    @Test
    void login_conCorreo_buscaPorCorreoCuandoNoHayUsuarioConEseNombre() {
        when(usuarioRepository.findByUsernameIgnoreCase("maria@mail.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreoIgnoreCase("maria@mail.com")).thenReturn(Optional.of(maria));
        when(passwordEncoder.matches("Clave1234", "hash-bcrypt")).thenReturn(true);
        when(jwtService.generarToken(maria)).thenReturn("token-falso");

        LoginResponse respuesta = usuarioService.login(new LoginRequest("maria@mail.com", "Clave1234"));

        assertThat(respuesta.user().correo()).isEqualTo("maria@mail.com");
    }

    // ---------- Caso LIMITE ----------

    @Test
    void login_conEspaciosAlrededorDelUsuario_losRecortaAntesDeBuscar() {
        when(usuarioRepository.findByUsernameIgnoreCase("maria")).thenReturn(Optional.of(maria));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generarToken(maria)).thenReturn("token-falso");

        usuarioService.login(new LoginRequest("   maria   ", "Clave1234"));

        verify(usuarioRepository).findByUsernameIgnoreCase("maria");
    }

    // ---------- Casos INVALIDOS / ERROR ----------

    @Test
    void login_conUsuarioInexistente_lanza401SinTocarElCifradorNiElToken() {
        when(usuarioRepository.findByUsernameIgnoreCase("nadie")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreoIgnoreCase("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest("nadie", "Clave1234")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode().value()).isEqualTo(401);
                    assertThat(ex.getReason()).isEqualTo("Usuario o contraseña incorrectos");
                });

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_conContrasenaIncorrecta_lanza401ConElMismoMensajeYNoGeneraToken() {
        when(usuarioRepository.findByUsernameIgnoreCase("maria")).thenReturn(Optional.of(maria));
        when(passwordEncoder.matches("incorrecta", "hash-bcrypt")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest("maria", "incorrecta")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode().value()).isEqualTo(401);
                    // Igual que con usuario inexistente: no se revela cual de los dos datos fallo
                    assertThat(ex.getReason()).isEqualTo("Usuario o contraseña incorrectos");
                });

        verify(jwtService, never()).generarToken(any());
    }
}
