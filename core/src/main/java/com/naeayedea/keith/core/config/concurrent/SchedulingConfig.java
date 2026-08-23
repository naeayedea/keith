package com.naeayedea.keith.core.config.concurrent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Backs channel session timeouts (e.g. a guess game's time limit) - one shared pool, since these
 * are lightweight, infrequent callbacks rather than a high-throughput workload.
 *
 * @author naeayedea
 */
@Configuration
public class SchedulingConfig {

    @Bean
    public ScheduledExecutorService sessionTimeoutScheduler() {
        return Executors.newScheduledThreadPool(2);
    }
}
