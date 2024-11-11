package com.fusionfx.monolith.security.token;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final TokenManagementService tokenManagementService;

    @Cacheable(cacheManager = "cacheManager", cacheNames = "tokenCache", sync = true)
    public TokenHolder getToken() throws Auth0Exception {
        return tokenManagementService.fetchNewToken();
    }

    @CacheEvict(cacheManager = "cacheManager", allEntries = true, cacheNames = "tokenCache")
    public void removeToken() {
        log.info("OrgIam | TokenCacheService | Cache Evict Completed Successfully");
    }

}
