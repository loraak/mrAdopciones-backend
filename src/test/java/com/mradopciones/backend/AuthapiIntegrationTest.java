package com.mradopciones.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1",
        "app.jwt.secret=clave-de-pruebas-con-mas-de-32-caracteres-123",
        "app.uploads-dir=target/test-uploads"
})
@Transactional
class AuthApiIntegrationTest {

    @Autowired private WebApplicationContext contexto;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    private void registrar(String usuario, String rol) throws Exception {
        String json = """
                {"username":"%s","email":"%s@mail.com","password":"Clave1234","role":"%s","phone":"4421234567"}
                """.formatted(usuario, usuario, rol);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    private String iniciarSesion(String usuario, String clave) throws Exception {
        String json = """
                {"username":"%s","password":"%s"}
                """.formatted(usuario, clave);
        String respuesta = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    @Test
    void flujoCompleto_registro_login_yRutaProtegida() throws Exception {
        registrar("ana", "ADOPTANTE");
        String token = iniciarSesion("ana", "Clave1234");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.password").doesNotExist());   // nunca se devuelve la contraseña
    }

    @Test
    void rutaProtegida_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void credencialesIncorrectas_devuelven401ConElMismoMensajeGenerico() throws Exception {
        registrar("ana", "ADOPTANTE");

        String malaClave = """
                {"username":"ana","password":"incorrecta1"}
                """;
        String noExiste = """
                {"username":"nadie","password":"Clave1234"}
                """;

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(malaClave))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(noExiste))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void tokenManipulado_devuelve401() throws Exception {
        registrar("ana", "ADOPTANTE");
        String token = iniciarSesion("ana", "Clave1234");
        String alterado = token.substring(0, token.length() - 10) + "AAAAAAAAAA";

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adoptante_noPuedePublicarMascotas_devuelve403() throws Exception {
        registrar("ana", "ADOPTANTE");
        String token = iniciarSesion("ana", "Clave1234");

        mockMvc.perform(post("/api/mascotas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}