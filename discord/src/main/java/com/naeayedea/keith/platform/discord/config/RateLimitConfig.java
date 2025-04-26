package com.naeayedea.keith.platform.discord.config;

import com.naeayedea.keith.core.ratelimiter.CommandRateLimiter;
import com.naeayedea.keith.core.ratelimiter.RateLimitCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Value("${keith.rate-limit.max-value}")
    private int rateLimitMax;

    @Bean
    public CommandRateLimiter commandRateLimiter(RateLimitCache rateLimitCache) {
        return new CommandRateLimiter(rateLimitCache, rateLimitMax);
    }
}
