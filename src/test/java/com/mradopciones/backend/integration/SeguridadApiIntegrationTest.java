package com.mradopciones.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** AUT-05 | Integración | Códigos HTTP y matriz de permisos de la API (CP-11, CP-14, CP-19, CP-21, CP-22). */
class SeguridadApiIntegrationTest extends ApiIntegrationBase {

    @Test
    void sinToken_solicitudesRecibidas_401() throws Exception {
        mockMvc.perform(get("/api/solicitudes/recibidas")).andExpect(status().isUnauthorized());
    }

    @Test
    void adoptante_noPuedeVerSolicitudesRecibidas_niSubirFotos_403() throws Exception {
        registrar("ana", "ADOPTANTE");
        String tAna = token("ana");

        mockMvc.perform(get("/api/solicitudes/recibidas").header(AUTH, bearer(tAna)))
                .andExpect(status().isForbidden());
        mockMvc.perform(multipart("/api/uploads/pets")
                        .file(new MockMultipartFile("file", "a.png", "image/png", "x".getBytes()))
                        .header(AUTH, bearer(tAna)))
                .andExpect(status().isForbidden());
    }

    @Test
    void donanteAjeno_noEditaNiEliminaMascota_403_yElDuenoSi() throws Exception {
        registrar("dona", "DONANTE");
        registrar("donb", "DONANTE");
        String tA = token("dona");
        String tB = token("donb");
        long id = crearMascota(tA);

        mockMvc.perform(put("/api/mascotas/" + id).header(AUTH, bearer(tB))
                        .contentType(MediaType.APPLICATION_JSON).content(mascotaJson(4)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Esta mascota no es tuya"));
        mockMvc.perform(delete("/api/mascotas/" + id).header(AUTH, bearer(tB)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/mascotas/" + id).header(AUTH, bearer(tA)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/mascotas/" + id)).andExpect(status().isNotFound());
    }

    /** La especificación (CP-22) decía 403; la API responde 404 a propósito para no revelar que el chat existe. */
    @Test
    void terceroUsuario_chatAjeno_404() throws Exception {
        registrar("dona", "DONANTE");
        registrar("ana", "ADOPTANTE");
        registrar("luis", "ADOPTANTE");
        String tDona = token("dona");
        long solicitudId = crearSolicitud(token("ana"), crearMascota(tDona));
        responder(tDona, solicitudId, "Aceptada").andExpect(status().isOk());

        mockMvc.perform(get("/api/requests/" + solicitudId + "/chat").header(AUTH, bearer(token("luis"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicarMascota_edadEnLosLimites() throws Exception {
        registrar("dona", "DONANTE");
        String t = token("dona");
        for (int edad : new int[]{0, 50}) {
            mockMvc.perform(post("/api/mascotas").header(AUTH, bearer(t))
                            .contentType(MediaType.APPLICATION_JSON).content(mascotaJson(edad)))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/mascotas").header(AUTH, bearer(t))
                        .contentType(MediaType.APPLICATION_JSON).content(mascotaJson(51)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La edad no es válida"));
        mockMvc.perform(post("/api/mascotas").header(AUTH, bearer(t))
                        .contentType(MediaType.APPLICATION_JSON).content(mascotaJson(-1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La edad no puede ser negativa"));
    }

    @Test
    void mascotaInexistente_404() throws Exception {
        mockMvc.perform(get("/api/mascotas/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mascota no encontrada con id 999999"));
    }

    @Test
    void subirFoto_gifEsRechazada_pngEsAceptada() throws Exception {
        registrar("dona", "DONANTE");
        String t = token("dona");

        mockMvc.perform(multipart("/api/uploads/pets")
                        .file(new MockMultipartFile("file", "a.gif", "image/gif", "x".getBytes()))
                        .header(AUTH, bearer(t)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Formato no permitido. Usa JPG, PNG o WEBP"));
        mockMvc.perform(multipart("/api/uploads/pets")
                        .file(new MockMultipartFile("file", "a.png", "image/png", "x".getBytes()))
                        .header(AUTH, bearer(t)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", startsWith("/uploads/")));
    }
}