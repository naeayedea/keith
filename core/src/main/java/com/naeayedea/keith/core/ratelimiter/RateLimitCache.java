package com.naeayedea.keith.core.ratelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class RateLimitCache {

    public static final String CACHE_NAME = "rate_limit_internal";

    @Cacheable(cacheNames = CACHE_NAME, cacheManager = "rateLimitCacheManager", key = "#id")
    public int getCurrentValue(String id) {
        //doesn't make sense at first glance, but if in cache, method not called, if not in cache then start with 0 rate limit value
        return 0;
    }

    @CachePut(cacheNames = CACHE_NAME, cacheManager = "rateLimitCacheManager", key = "#id")
    public int setCurrentValue(String id, int value) {
        return value;
    }

    @CacheEvict(cacheNames = CACHE_NAME, cacheManager = "rateLimitCacheManager", key = "#id")
    public void clearRecord(String id) {
        //side effects here
    }

    @CacheEvict(cacheNames = CACHE_NAME, cacheManager = "rateLimitCacheManager")
    public void clearEntries() {
        //side effects here
    }
}
