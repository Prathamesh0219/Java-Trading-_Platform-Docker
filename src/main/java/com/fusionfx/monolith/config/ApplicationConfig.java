package com.fusionfx.monolith.config;

import cloud.metaapi.sdk.meta_api.MetaApi;
import com.fusionfx.monolith.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ApplicationConfig {

    @Bean
    MetaApi metaApi(final AppProperties appProperties) throws IOException {
        return new MetaApi(appProperties.getMetaApi().getApiKey());
    }

}
