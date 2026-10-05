package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.entities.Usuario;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

@Service
public class JWTService {
    public record TokenData(Long userId, String username, Rol rol) {}

    private final byte[] secret;
    private final long expirationMs;

    public JWTService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");

        }
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        try {
            Date ahora = new Date();
            JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(String.valueOf(usuario.getId())).claim("username", usuario.getUsername()).claim("rol", usuario.getRol().name()).issueTime(ahora).expirationTime(new Date(ahora.getTime() + expirationMs)).build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo hacer el token", e);
        }
    }

    public TokenData validar(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())
                    || !jwt.verify(new MACVerifier(secret))) {
                throw noAutorizado("Token inválido");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw noAutorizado("La sesión expiró, inicia sesión de nuevo");
            }

            return new TokenData(
                    Long.valueOf(claims.getSubject()),
                    claims.getStringClaim("username"),
                    Rol.valueOf(claims.getStringClaim("rol"))
            );
        } catch (ParseException | JOSEException | IllegalArgumentException e) {
            throw noAutorizado("Token inválido");
        }
    }

    private ResponseStatusException noAutorizado(String mensaje) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, mensaje);
    }
}
