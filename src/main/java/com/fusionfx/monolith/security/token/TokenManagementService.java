package com.fusionfx.monolith.security.token;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.fusionfx.monolith.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenManagementService {

    private final AuthAPI authAPI;
    private final AppProperties appProperties;

    public TokenHolder fetchNewToken() throws Auth0Exception {
        var tokenRequest = authAPI.requestToken(appProperties.getAuth0().getAudience());
        var tokenHolder = tokenRequest.execute().getBody();
        log.info("OrgIam | TokenManagementService | Fetch New Token Completed Successfully");
        return tokenHolder;
    }

}
