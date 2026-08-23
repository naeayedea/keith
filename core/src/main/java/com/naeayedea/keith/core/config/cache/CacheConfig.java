package com.naeayedea.keith.core.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${keith.manager.cache.refresh}")
    private long DEFAULT_CACHE_EXPIRY_SECONDS;

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfter(new Expiry<>() {
            @Override
            public long expireAfterCreate(Object key, Object value, long currentTime) {
                return TimeUnit.SECONDS.toNanos(DEFAULT_CACHE_EXPIRY_SECONDS);
            }

            @Override
            public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
                //reset on update
                return expireAfterCreate(key, value, currentTime);
            }

            @Override
            public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
                //reset on read
                return expireAfterCreate(key, value, currentTime);
            }
        }));

        return cacheManager;
    }

}
