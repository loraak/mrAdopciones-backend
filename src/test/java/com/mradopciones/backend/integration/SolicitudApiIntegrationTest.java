package com.mradopciones.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** AUT-04 | Integración | Flujo solicitud -> respuesta -> chat sobre la API real (CP-13, CP-18). */
class SolicitudApiIntegrationTest extends ApiIntegrationBase {

    @Test
    void flujoCompleto_aceptar_sacaDelCatalogo_yHabilitaElChat() throws Exception {
        registrar("dona", "DONANTE");
        registrar("ana", "ADOPTANTE");
        String tDona = token("dona");
        String tAna = token("ana");
        long mascotaId = crearMascota(tDona);
        long solicitudId = crearSolicitud(tAna, mascotaId);

        // Antes de aceptar el chat no existe
        mockMvc.perform(get("/api/requests/" + solicitudId + "/chat").header(AUTH, bearer(tAna)))
                .andExpect(status().isForbidden());

        responder(tDona, solicitudId, "Aceptada")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("Aceptada"));

        mockMvc.perform(get("/api/mascotas/" + mascotaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("En proceso"));
        mockMvc.perform(get("/api/mascotas"))
                .andExpect(jsonPath("$[?(@.id == " + mascotaId + ")]").isEmpty());

        // Ya aceptada, ambos participantes chatean
        mockMvc.perform(get("/api/requests/" + solicitudId + "/chat").header(AUTH, bearer(tAna)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otroNombre").value("dona"));
        mockMvc.perform(post("/api/requests/" + solicitudId + "/messages")
                        .header(AUTH, bearer(tAna)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenido\":\"Hola, ¿cuándo puedo conocerlo?\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/requests/" + solicitudId + "/messages").header(AUTH, bearer(tDona)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].propio").value(false))
                .andExpect(jsonPath("$[0].emisorNombre").value("ana"));
    }

    @Test
    void responderDosVeces_laMismaSolicitud_409() throws Exception {
        registrar("dona", "DONANTE");
        registrar("ana", "ADOPTANTE");
        String tDona = token("dona");
        long solicitudId = crearSolicitud(token("ana"), crearMascota(tDona));

        responder(tDona, solicitudId, "Rechazada").andExpect(status().isOk());
        responder(tDona, solicitudId, "Aceptada")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Esta solicitud ya fue respondida"));
    }

    /** Defecto D2 (CP-18): dos adoptantes piden la misma mascota; solo una puede aceptarse. */
    @Test
    void aceptarDosSolicitudesDeLaMismaMascota_laSegundaEs409() throws Exception {
        registrar("dona", "DONANTE");
        registrar("ana", "ADOPTANTE");
        registrar("luis", "ADOPTANTE");
        String tDona = token("dona");
        long mascotaId = crearMascota(tDona);
        long deAna = crearSolicitud(token("ana"), mascotaId);
        long deLuis = crearSolicitud(token("luis"), mascotaId);

        responder(tDona, deAna, "Aceptada").andExpect(status().isOk());
        responder(tDona, deLuis, "Aceptada").andExpect(status().isConflict());
    }

    @Test
    void solicitudDuplicadaPendiente_yMensajeCorto_sonRechazados() throws Exception {
        registrar("dona", "DONANTE");
        registrar("ana", "ADOPTANTE");
        String tAna = token("ana");
        long mascotaId = crearMascota(token("dona"));
        crearSolicitud(tAna, mascotaId);

        solicitar(tAna, mascotaId, "Otra vez la quiero")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya tienes una solicitud pendiente para esta mascota"));
        solicitar(tAna, mascotaId, "123456789")   // 9 caracteres (CP-13)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El mensaje debe tener entre 10 y 1000 caracteres"));
    }
}