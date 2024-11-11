package com.fusionfx.monolith.service.auth;

import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.model.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DxTradeAuthService {

    private final CacheManager cacheManager;
    private final WebClient.Builder webClientBuilder;

    // Retrieve the token map for a user from the cache, or create a new one
    @SuppressWarnings("unchecked")
    private Map<String, TokenInfo> getTokenMapForUser(String userId) {
        Map<String, TokenInfo> tokenMap = cacheManager.getCache("userTokens")
                .get(userId, HashMap.class);

        if (tokenMap == null) {
            tokenMap = new HashMap<>();
            cacheManager.getCache("userTokens").put(userId, tokenMap);
        }

        return tokenMap;
    }

    // Login and get a new access token (baseUrl is passed dynamically)
    public Mono<String> login(TradeAccount tradeAccount) {
        Map<String, String> requestBody = Map.of(
                "username", tradeAccount.getUsername(),
                "password", tradeAccount.getPassword(),
                "domain", "default"
        );

        log.info("DX Trade | Performing Login for {}", tradeAccount.getAccountNumber());

        // Use dynamic baseUrl for WebClient
        WebClient webClient = webClientBuilder.baseUrl(tradeAccount.getConnectionUrl()).build();

        return webClient.post()
                .uri("/dxsca-web/login")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String sessionToken = (String) response.get("sessionToken");

                    if (sessionToken == null) {
                        throw new RuntimeException("No access token returned");
                    }

                    // Store tokens in the cache
                    Map<String, TokenInfo> tokenMap = getTokenMapForUser(tradeAccount.getUserId());
                    tokenMap.put(tradeAccount.getAccountNumber(), TokenInfo.builder()
                            .accessToken(sessionToken)
                            .expiryTime(Instant.now().plusSeconds(1700L).toEpochMilli())
                            .build());

                    // Update the cache with the new token map
                    cacheManager.getCache("userTokens").put(tradeAccount.getUserId(), tokenMap);

                    return sessionToken;
                });
    }

    // Refresh the access token using the refresh token (baseUrl is passed dynamically)
    public Mono<String> refreshAccessToken(TradeAccount tradeAccount) {
        Map<String, TokenInfo> tokenMap = getTokenMapForUser(tradeAccount.getUserId());
        TokenInfo tokenInfo = tokenMap.get(tradeAccount.getAccountNumber());

        if (tokenInfo == null || tokenInfo.getRefreshToken() == null) {
            throw new RuntimeException("No refresh token available for account: " + tradeAccount.getAccountNumber());
        }

        Map<String, String> requestBody = Map.of("refresh_token", tokenInfo.getRefreshToken());

        // Use dynamic baseUrl for WebClient
        WebClient webClient = webClientBuilder.baseUrl(tradeAccount.getConnectionUrl()).build();

        return webClient.post()
                .uri("/auth/jwt/refresh")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String newAccessToken = (String) response.get("accessToken");
                    Instant expiresAt = Instant.parse((String) response.get("expireDate"));

                    if (newAccessToken == null) {
                        throw new RuntimeException("No access token returned after refresh");
                    }

                    // Update token info in the cache
                    tokenInfo.setAccessToken(newAccessToken);
                    tokenInfo.setExpiryTime(expiresAt.toEpochMilli());

                    // Update the cache with new token info
                    cacheManager.getCache("userTokens").put(tradeAccount.getUserId(), tokenMap);

                    return newAccessToken;
                });
    }

    // Get a valid token (login or refresh if necessary, with dynamic baseUrl)
    public Mono<String> getValidToken(final TradeAccount tradeAccount) {
        Map<String, TokenInfo> tokenMap = getTokenMapForUser(tradeAccount.getUserId());
        TokenInfo tokenInfo = tokenMap.get(tradeAccount.getAccountNumber());

        // If token is missing or expired, refresh or login
        if (tokenInfo == null || tokenInfo.isExpired()) {
            if (tokenInfo != null && tokenInfo.getRefreshToken() != null) {
                return refreshAccessToken(tradeAccount);
            } else {
                return login(tradeAccount);
            }
        }

        // Return the cached valid access token
        return Mono.just(tokenInfo.getAccessToken());
    }

}
