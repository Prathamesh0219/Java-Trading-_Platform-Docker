package com.fusionfx.monolith.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenUtil {

    private final JwtDecoder jwtDecoder;

    /**
     * Extracts the userId from the JWT token.
     *
     * @param token The JWT token.
     * @return The userId if present, otherwise null.
     */
    public String extractUserId(String token) {
        // Decode the JWT token
        Jwt decodedJwt = jwtDecoder.decode(token);

        // Extract the userId from the token claims
        // This assumes the userId is stored as the "sub" (subject) claim
        return decodedJwt.getClaim("sub");
    }
}
