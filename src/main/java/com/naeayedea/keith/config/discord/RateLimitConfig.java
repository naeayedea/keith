package com.naeayedea.keith.config.discord;

import com.naeayedea.keith.ratelimiter.CommandRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Value("${keith.rateLimit.maxValue}")
    private int rateLimitMax;

    @Bean
    public CommandRateLimiter commandRateLimiter() {
        return new CommandRateLimiter(rateLimitMax);
    }
}
