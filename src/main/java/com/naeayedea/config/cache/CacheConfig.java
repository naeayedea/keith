package com.naeayedea.config.cache;

import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.managers.UserManager;
import com.naeayedea.keith.ratelimiter.CommandRateLimiter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    private final ScheduledExecutorService scheduledExecutorService;

    private final ServerManager serverManager;

    private final UserManager userManager;

    private final CommandRateLimiter rateLimiter;

    @Value("${keith.manager.cache.refresh}")
    private int cacheRefreshIntervalInSeconds;

    public CacheConfig(@Qualifier("scheduler")ScheduledExecutorService scheduledExecutorService, ServerManager serverManager, UserManager userManager, CommandRateLimiter rateLimiter) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.serverManager = serverManager;
        this.userManager = userManager;
        this.rateLimiter = rateLimiter;
    }

    @PostConstruct
    public void configureManagerCleanup() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            rateLimiter.clearEntries();
            serverManager.clear();
            userManager.clear();
        }, cacheRefreshIntervalInSeconds, cacheRefreshIntervalInSeconds, TimeUnit.SECONDS);
    }
}
