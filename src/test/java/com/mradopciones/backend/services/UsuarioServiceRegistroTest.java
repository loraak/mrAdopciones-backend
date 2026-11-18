package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.RegistroRequest;
import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** AUT-02 | Unitarias | Regla de negocio UsuarioService.registrar (CP-06, CP-07). */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceRegistroTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JWTService jwtService;

    @InjectMocks private UsuarioService usuarioService;

    private void assertEstatus(Throwable error, int esperado) {
        assertThat(error).isInstanceOfSatisfying(ResponseStatusException.class,
                ex -> assertThat(ex.getStatusCode().value()).isEqualTo(esperado));
    }

    @Test
    void adoptanteValido_seGuardaConClaveCifradaYDatosLimpios() {
        when(passwordEncoder.encode("Clave1234")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        usuarioService.registrar(new RegistroRequest(" Ana ", "ANA@Mail.COM", "Clave1234",
                Rol.ADOPTANTE, " 4421234567 ", "Estudiante", null, null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getUsername()).isEqualTo("Ana");
        assertThat(guardado.getCorreo()).isEqualTo("ana@mail.com");
        assertThat(guardado.getContrasenia()).isEqualTo("hash-bcrypt").isNotEqualTo("Clave1234");
        assertThat(guardado.getTelefono()).isEqualTo("4421234567");
        assertThat(guardado.getLocacion()).isNull();
    }

    @Test
    void donante_conCamposVacios_losGuardaComoNulos() {
        when(passwordEncoder.encode("Clave1234")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario u = usuarioService.registrar(new RegistroRequest("dona", "dona@mail.com", "Clave1234",
                Rol.DONANTE, null, null, "  Querétaro ", "   "));

        assertThat(u.getLocacion()).isEqualTo("Querétaro");
        assertThat(u.getOrganizacion()).isNull();
        assertThat(u.getTelefono()).isNull();
    }

    @Test
    void rolAdmin_noSePermite_403() {
        Throwable e = catchThrowable(() -> usuarioService.registrar(new RegistroRequest(
                "root", "root@mail.com", "Clave1234", Rol.ADMIN, null, null, null, null)));

        assertEstatus(e, 403);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void adoptanteSinTelefonoNulo_oSinOcupacionVacia_400() {
        Throwable sinTel = catchThrowable(() -> usuarioService.registrar(new RegistroRequest(
                "ana", "ana@mail.com", "Clave1234", Rol.ADOPTANTE, null, "Estudiante", null, null)));
        Throwable sinOcup = catchThrowable(() -> usuarioService.registrar(new RegistroRequest(
                "ana", "ana@mail.com", "Clave1234", Rol.ADOPTANTE, "4421234567", "   ", null, null)));

        assertEstatus(sinTel, 400);
        assertEstatus(sinOcup, 400);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void correoRepetido_sinImportarMayusculas_409() {
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@mail.com")).thenReturn(true);

        Throwable e = catchThrowable(() -> usuarioService.registrar(new RegistroRequest(
                "otra", "ANA@MAIL.com", "Clave1234", Rol.ADOPTANTE, "4421234567", "Estudiante", null, null)));

        assertEstatus(e, 409);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void usuarioRepetido_409() {
        when(usuarioRepository.existsByUsernameIgnoreCase("ana")).thenReturn(true);

        Throwable e = catchThrowable(() -> usuarioService.registrar(new RegistroRequest(
                " ana ", "otro@mail.com", "Clave1234", Rol.ADOPTANTE, "4421234567", "Estudiante", null, null)));

        assertEstatus(e, 409);
        verify(usuarioRepository, never()).save(any());
    }
}