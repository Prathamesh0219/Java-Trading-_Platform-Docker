package com.fusionfx.monolith.security.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import com.fusionfx.monolith.AppProperties;
import com.fusionfx.monolith.dto.Auth0TokenResponse;
import com.fusionfx.monolith.dto.CreateUserRequest;
import com.fusionfx.monolith.entity.UserAccount;
import com.fusionfx.monolith.repo.UserAccountRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AuthService {

    private final WebClient webClient;
    private final AppProperties appProperties;
    private final ManagementAPI managementAPI;
    private final UserAccountRepo userAccountRepo;

    public AuthService(final AppProperties appProperties,
                       final ManagementAPI managementAPI,
                       final UserAccountRepo userAccountRepo) {
        this.appProperties = appProperties;
        this.managementAPI = managementAPI;
        this.userAccountRepo = userAccountRepo;
        this.webClient = WebClient.builder()
                .baseUrl(appProperties.getAuth0().getIssuer())
                .build();
    }

    /**
     * Creates the user in Auth0, saves the user in MongoDB, and logs the user in.
     *
     * @param createUserRequest The user's details for account creation
     * @return A Mono containing the Auth0TokenResponse (JWT Token)
     */
    public Mono<Auth0TokenResponse> createUserAccount(final CreateUserRequest createUserRequest) {
        return Mono.fromCallable(() -> createAuth0UserAccount(createUserRequest))
                .map(auth0UserId -> {
                    // Create a UserAccount object to save in MongoDB
                    UserAccount userAccount = new UserAccount();
                    userAccount.setUserId(auth0UserId);  // Set Auth0 userId in MongoDB
                    userAccount.setFirstName(createUserRequest.getFirstName());
                    userAccount.setLastName(createUserRequest.getLastName());
                    userAccount.setEmail(createUserRequest.getEmail());

                    // Save the user in MongoDB and return Mono<UserAccount>
                    return userAccountRepo.save(userAccount);
                })
                .flatMap(savedUserAccount -> {
                    // Authenticate (log in) the user and return the token as Mono<Auth0TokenResponse>
                    return authenticate(createUserRequest.getEmail(), createUserRequest.getPassword());
                })
                .doOnError(error -> log.error("Error creating user account: {}", error.getMessage()));
    }

    /**
     * Authenticate the user and retrieve the Auth0 token response.
     */
    public Mono<Auth0TokenResponse> authenticate(final String email, final String password) {
        return webClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createAuthRequestBody(email, password))
                .retrieve()
                .bodyToMono(Auth0TokenResponse.class);
    }

    /**
     * Private method to create a user in Auth0.
     *
     * @param request The user's details for account creation
     * @return The Auth0 user ID of the created user
     */
    private String createAuth0UserAccount(final CreateUserRequest request)
            throws Auth0Exception {

        var userToCreate = new User(appProperties.getAuth0().getConnection());
        userToCreate.setName(request.getFirstName());
        userToCreate.setFamilyName(request.getLastName());
        userToCreate.setEmail(request.getEmail());
        userToCreate.setPassword(request.getPassword().toCharArray());


        var authUser = managementAPI.users().create(userToCreate).execute().getBody();
        return authUser.getId();  // Return Auth0 user ID
    }

    /**
     * Helper method to create the body for the authentication request.
     */
    private Map<String, String> createAuthRequestBody(final String email, final String password) {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "password");
        body.put("username", email);
        body.put("password", password);
        body.put("client_id", appProperties.getAuth0().getClientId());
        body.put("client_secret", appProperties.getAuth0().getClientSecret());
        body.put("audience", "https://fusion/");
        body.put("connection", "Username-Password-Authentication");
        body.put("scope", "offline_access");
        return body;
    }
}
