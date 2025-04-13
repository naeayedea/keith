package com.naeayedea.keith.config.concurrent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ExecutorServiceConfig {

    private static final int THREAD_KEEP_ALIVE_SECONDS = 60;

    @Value("${keith.executor.messages.pool-size.min}")
    private int messagesMinPoolSize;

    @Value("${keith.executor.messages.pool-size.max}")
    private int messagesMaxCorePoolSize;

    @Value("${keith.executor.reactions.pool-size.min}")
    private int reactionsMinPoolSize;

    @Value("${keith.executor.reactions.pool-size.max}")
    private int reactionsMaxPoolSize;

    @Value("${keith.executor.commands.pool-size.min}")
    private int commandsMinPoolSize;

    @Value("${keith.executor.commands.pool-size.max}")
    private int commandsMaxPoolSize;

    @Value("${keith.executor.scheduler.pool-size.core}")
    private int schedulerCorePoolSize;

    @Bean
    public ExecutorService messageService() {
        return new ThreadPoolExecutor(messagesMinPoolSize, messagesMaxCorePoolSize, THREAD_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @Bean
    public ExecutorService reactionService() {
        return new ThreadPoolExecutor(reactionsMinPoolSize, reactionsMaxPoolSize, THREAD_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @Bean
    public ExecutorService commandService() {
        return new ThreadPoolExecutor(commandsMinPoolSize, commandsMaxPoolSize, THREAD_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @Bean
    public ScheduledExecutorService scheduler() {
        return new ScheduledThreadPoolExecutor(schedulerCorePoolSize);
    }
}
