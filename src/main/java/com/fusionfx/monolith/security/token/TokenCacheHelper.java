package com.fusionfx.monolith.security.token;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenCacheHelper {

    private final TokenCacheService tokenCacheService;

    public TokenCacheHelper(final TokenCacheService tokenCacheService) {
        this.tokenCacheService = tokenCacheService;
    }

    private TokenHolder fetchNewToken() throws Auth0Exception {
        var tokenHolder = tokenCacheService.getToken();
        var refreshToken = tokenHolder.getExpiresIn() < 600_000;
        if (refreshToken) {
            log.info("OrgIam | TokenCacheHelper | Token expiring in less than 600000ms, Commencing Refresh");
            tokenCacheService.removeToken();
            return tokenCacheService.getToken();
        }
        return tokenHolder;
    }

    public void validateUnexpiredToken(final ManagementAPI managementAPI) throws Auth0Exception {
        var token = fetchNewToken().getAccessToken();
        managementAPI.setApiToken(token);
    }

}
