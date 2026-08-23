package com.naeayedea.keith.platform.discord.ratelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(CommandRateLimiter.class);

    public final int DEFAULT_RATE_LIMIT;

    private final RateLimitCache rateLimitCache;

    public CommandRateLimiter(RateLimitCache rateLimitCache, int rateLimitMax) {
        this.DEFAULT_RATE_LIMIT = rateLimitMax;
        this.rateLimitCache = rateLimitCache;

        if (rateLimitMax <= 0) {
            logger.error("Default rate limit less than or equal to 0, value: {}. Users wont be able run commands by default", rateLimitMax);
            throw new IllegalStateException("Default rate limit less than or equal to 0");
        }
    }

    public int getCurrentValue(String id) {
        return rateLimitCache.getCurrentValue(id);
    }

    public boolean userPermitted(String id) {
        return this.userPermitted(id, DEFAULT_RATE_LIMIT);
    }

    public boolean userPermitted(String id, int rateLimitMax) {
        int value = rateLimitCache.getCurrentValue(id);

        logger.trace("Loaded value {} from cache for user {}", value, id);

        return value < rateLimitMax;
    }

    public void incrementOrInsertRecord(String id, int incrementAmount) {
        int currentValue = rateLimitCache.getCurrentValue(id);
        int updatedValue = currentValue + incrementAmount;

        logger.trace("Incrementing rate limit for user {}, current {}, updated: {}", id, currentValue, updatedValue);

        rateLimitCache.setCurrentValue(id, updatedValue);
    }

    public void clearRecord(String id) {
        logger.debug("Clearing user {} from the rate limit cache", id);

        rateLimitCache.getCurrentValue(id);
    }

    public void clearEntries() {
        logger.debug("Clearing all entries from the rate limit cache");

        rateLimitCache.clearEntries();
    }
}
