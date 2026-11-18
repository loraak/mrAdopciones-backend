package com.mradopciones.backend.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Base común: API completa (controlador + interceptor + servicio + JPA + H2 en memoria). */
@Tag("integration")
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1",
        "app.jwt.secret=clave-de-pruebas-con-mas-de-32-caracteres-123",
        "app.uploads-dir=target/test-uploads"
})
@Transactional
abstract class ApiIntegrationBase {

    protected static final String AUTH = "Authorization";

    @Autowired protected WebApplicationContext contexto;
    protected MockMvc mockMvc;

    @BeforeEach
    void prepararMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String mascotaJson(int edad) {
        return """
            {"nombre":"Firulais","edad":%d,"tipo":"perros","descripcion":"Muy jugueton","imagen":"",
             "etiquetas":["sano","jugueton"]}
            """.formatted(edad);
    }

    protected void registrar(String usuario, String rol) throws Exception {
        String json = """
            {"username":"%s","correo":"%s@mail.com","contrasena":"Clave1234","rol":"%s",
             "telefono":"4421234567","ocupacion":"Estudiante"}
            """.formatted(usuario, usuario, rol);
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    protected String token(String usuario) throws Exception {
        String json = """
            {"username":"%s","contrasena":"Clave1234"}
            """.formatted(usuario);
        String resp = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resp, "$.token");
    }

    protected long crearMascota(String tokenDonante) throws Exception {
        String resp = mockMvc.perform(post("/api/mascotas")
                        .header(AUTH, bearer(tokenDonante))
                        .contentType(MediaType.APPLICATION_JSON).content(mascotaJson(3)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resp, "$.id")).longValue();
    }

    protected ResultActions solicitar(String tokenAdoptante, long mascotaId, String mensaje) throws Exception {
        return mockMvc.perform(post("/api/solicitudes")
                .header(AUTH, bearer(tokenAdoptante))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mascotaId\":" + mascotaId + ",\"mensaje\":\"" + mensaje + "\"}"));
    }

    protected long crearSolicitud(String tokenAdoptante, long mascotaId) throws Exception {
        String resp = solicitar(tokenAdoptante, mascotaId, "Quiero adoptarlo, tengo patio")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resp, "$.id")).longValue();
    }

    protected ResultActions responder(String tokenDonante, long solicitudId, String estatus) throws Exception {
        return mockMvc.perform(patch("/api/solicitudes/" + solicitudId + "/estatus")
                .header(AUTH, bearer(tokenDonante))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estatus\":\"" + estatus + "\"}"));
    }
}