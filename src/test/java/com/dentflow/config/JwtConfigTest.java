package com.dentflow.config;

import com.dentflow.model.Role;
import com.dentflow.model.User;
import com.dentflow.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtConfigTest {

    @Test
    void generatedTokenIsSignedAndValidatedByTheBackend() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtProperties properties = new JwtProperties(secret, "http://localhost:8080", Duration.ofMinutes(30));
        JwtConfig config = new JwtConfig();
        SecretKey key = config.jwtSecretKey(properties);
        JwtEncoder encoder = config.jwtEncoder(key);
        JwtDecoder decoder = config.jwtDecoder(key, properties);
        JwtService jwtService = new JwtService(encoder, properties);

        User user = new User();
        user.setId(42L);
        user.setUsername("dentflow-user");
        user.setEmail("user@dentflow.test");
        user.setRole(Role.USER);

        Jwt jwt = decoder.decode(jwtService.generateToken(user));

        assertEquals("42", jwt.getSubject());
        assertEquals("http://localhost:8080", jwt.getIssuer().toString());
        assertEquals("user@dentflow.test", jwt.getClaimAsString("email"));
        assertEquals("USER", jwt.getClaimAsString("role"));
    }
}
