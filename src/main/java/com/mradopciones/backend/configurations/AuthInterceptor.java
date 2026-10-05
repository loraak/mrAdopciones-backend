package com.mradopciones.backend.configurations;

import com.mradopciones.backend.services.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JWTService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Preflight de CORS, recursos estáticos, etc.
        if (!(handler instanceof HandlerMethod metodo)) {
            return true;
        }

        RequiereSesion regla = metodo.getMethodAnnotation(RequiereSesion.class);
        if (regla == null) {
            regla = metodo.getBeanType().getAnnotation(RequiereSesion.class);
        }
        if (regla == null) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }

        JWTService.TokenData datos = jwtService.validar(header.substring(7));

        if (regla.roles().length > 0 && !Arrays.asList(regla.roles()).contains(datos.rol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
        }

        request.setAttribute("userId", datos.userId());
        request.setAttribute("userRol", datos.rol());
        return true;
    }
}