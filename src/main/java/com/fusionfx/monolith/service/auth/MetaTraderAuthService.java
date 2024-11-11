package com.fusionfx.monolith.service.auth;

import com.fusionfx.monolith.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaTraderAuthService {

    private final AppProperties appProperties;


    private final WebClient.Builder webClient;

    public Mono<String> getAuthToken(final String accountId, int validityInHours) {
        // Prepare the JSON body with explicit access to metaapi-api streaming
        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("applications", new String[]{"metaapi-api"});  // Allow access to `metaapi-api`
        jsonBody.put("resources", new Map[]{
                Map.of("entity", "account", "id", accountId)  // Restrict to a specific account
        });
        jsonBody.put("methodGroups", new Map[]{
                Map.of(
                        "group", "streaming-api",
                        "methods", new String[]{"subscribe"}  // Allow access to `subscribe` method for streaming
                )
        });

        return webClient.build().post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("profile-api-v1.agiliumtrade.agiliumtrade.ai")
                        .path("/users/current/narrow-down-auth-token")
                        .queryParam("validity-in-hours", validityInHours)
                        .build())
                .header("auth-token", appProperties.getMetaApi().getApiKey())  // Add the custom auth-token header
                .bodyValue(new Object[]{jsonBody}) // Pass the JSON body as an array of objects
                .retrieve()
                .bodyToMono(String.class); // Adjust the response type based on what you expect
    }


}
