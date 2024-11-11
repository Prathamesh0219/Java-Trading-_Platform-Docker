package com.fusionfx.monolith;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.data.mongodb.core.query.Meta;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(value = "fusion")
public class AppProperties {

    @NestedConfigurationProperty
    private final Auth0 auth0 = new Auth0();

    @NestedConfigurationProperty
    private final MetaApi metaApi = new MetaApi();

    @Getter
    @Setter
    public static class Auth0 {
        private String issuer;
        private String clientId;
        private String audience;
        private String connection;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class MetaApi {
        private String apiKey;
    }

}
