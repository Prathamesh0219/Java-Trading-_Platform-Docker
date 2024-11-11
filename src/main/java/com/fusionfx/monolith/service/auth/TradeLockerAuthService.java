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
public class TradeLockerAuthService {

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
    public Mono<String> login(TradeAccount tradeAccount, String baseUrl) {
        Map<String, String> requestBody = Map.of(
                "email", tradeAccount.getEmail(),
                "password", tradeAccount.getPassword(),
                "server", tradeAccount.getServer()
        );

        log.info("TradeLocker | Performing Login for {}", tradeAccount.getAccountNumber());

        // Use dynamic baseUrl for WebClient
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        return webClient.post()
                .uri("/auth/jwt/token")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String accessToken = (String) response.get("accessToken");
                    String refreshToken = (String) response.get("refreshToken");
                    Instant expiresAt = Instant.parse((String) response.get("expireDate"));

                    if (accessToken == null) {
                        throw new RuntimeException("No access token returned");
                    }

                    // Store tokens in the cache
                    Map<String, TokenInfo> tokenMap = getTokenMapForUser(tradeAccount.getUserId());
                    tokenMap.put(tradeAccount.getAccountNumber(), TokenInfo.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .expiryTime(expiresAt.toEpochMilli())
                            .build());

                    // Update the cache with the new token map
                    cacheManager.getCache("userTokens").put(tradeAccount.getUserId(), tokenMap);

                    return accessToken;
                });
    }

    // Refresh the access token using the refresh token (baseUrl is passed dynamically)
    public Mono<String> refreshAccessToken(TradeAccount tradeAccount, String baseUrl) {
        Map<String, TokenInfo> tokenMap = getTokenMapForUser(tradeAccount.getUserId());
        TokenInfo tokenInfo = tokenMap.get(tradeAccount.getAccountNumber());

        if (tokenInfo == null || tokenInfo.getRefreshToken() == null) {
            throw new RuntimeException("No refresh token available for account: " + tradeAccount.getAccountNumber());
        }

        Map<String, String> requestBody = Map.of("refresh_token", tokenInfo.getRefreshToken());

        // Use dynamic baseUrl for WebClient
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

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
    public Mono<String> getValidToken(final TradeAccount tradeAccount, final String baseUrl) {
        Map<String, TokenInfo> tokenMap = getTokenMapForUser(tradeAccount.getUserId());
        TokenInfo tokenInfo = tokenMap.get(tradeAccount.getAccountNumber());

        // If token is missing or expired, refresh or login
        if (tokenInfo == null || tokenInfo.isExpired()) {
            if (tokenInfo != null && tokenInfo.getRefreshToken() != null) {
                return refreshAccessToken(tradeAccount, baseUrl);
            } else {
                return login(tradeAccount, baseUrl);
            }
        }

        // Return the cached valid access token
        return Mono.just(tokenInfo.getAccessToken());
    }
}
