package com.mradopciones.backend.controllers;

import com.mradopciones.backend.configurations.RequiereSesion;
import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @RequiereSesion(roles = Rol.DONANTE)
    @PostMapping(value = "/pets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> subirFotoMascota(@RequestParam("file") MultipartFile file) {
        return Map.of("url", fileStorageService.guardarImagen(file));
    }
}