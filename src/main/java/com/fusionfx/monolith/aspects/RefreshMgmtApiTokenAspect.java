package com.fusionfx.monolith.aspects;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.fusionfx.monolith.security.token.TokenCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RefreshMgmtApiTokenAspect {

    private final ManagementAPI managementAPI;
    private final TokenCacheHelper tokenCacheHelper;

    public RefreshMgmtApiTokenAspect(final TokenCacheHelper tokenCacheHelper,
                                     final ManagementAPI managementAPI) {
        this.tokenCacheHelper = tokenCacheHelper;
        this.managementAPI = managementAPI;
    }

    @Before("@annotation(com.fusionfx.monolith.abstractions.RefreshManagementApiToken)")
    public void refreshManagementApiToken() throws Auth0Exception {
        tokenCacheHelper.validateUnexpiredToken(managementAPI);
    }

}
