package com.fusionfx.monolith.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Create ConcurrentMapCache for "tokenCache"
        var concurrentMapCache = new ConcurrentMapCache("tokenCache");

        // Create Caffeine cache for "userTokens"
        var caffeineCache = new CaffeineCache("userTokens", caffeineCacheBuilder().build());

        // Use SimpleCacheManager to manage both caches
        var simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(Arrays.asList(concurrentMapCache, caffeineCache));

        return simpleCacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(20, TimeUnit.DAYS)
                .maximumSize(100000);
    }

}
