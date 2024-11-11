package com.fusionfx.monolith.controller.User;

import com.fusionfx.monolith.abstractions.RefreshManagementApiToken;
import com.fusionfx.monolith.dto.Auth0TokenResponse;
import com.fusionfx.monolith.dto.CreateUserRequest;
import com.fusionfx.monolith.dto.LoginRequest;
import com.fusionfx.monolith.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class SecurityController {

    private final AuthService authService;

    /**
     * Login endpoint for user authentication.
     *
     * @param loginRequest The user's login details (email and password) in the request body
     * @return A Mono containing the JWT token response
     */
    @PostMapping("/login")
    public Mono<Auth0TokenResponse> login(@RequestBody LoginRequest loginRequest) {

        return authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword())
                .doOnError(WebClientResponseException.class, e -> {
                    log.error("Authentication Error: " + e.getResponseBodyAsString());
                });
    }

    /**
     * Endpoint to create a new Auth0 user account.
     *
     * @param createUserRequest The user's details for account creation in the request body
     * @return The created user's Auth0 ID
     */
    @PostMapping("/signup")
    @RefreshManagementApiToken
    public Mono<Auth0TokenResponse> createUser(@RequestBody CreateUserRequest createUserRequest) {
        return authService.createUserAccount(createUserRequest)
                .doOnError(WebClientResponseException.class, e -> {
                    log.error("Authentication Error: " + e.getResponseBodyAsString());
                });
    }
}

