package com.naeayedea.keith.platform.discord.config;

import com.naeayedea.keith.common.model.event.KeithEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class JDAEventPublishingConfig {

    private final Logger logger = LoggerFactory.getLogger(JDAEventPublishingConfig.class);

    private final ApplicationEventPublisher publisher;

    private final JDA jda;

    public JDAEventPublishingConfig(ApplicationEventPublisher publisher, JDA jda) {
        this.publisher = publisher;
        this.jda = jda;
    }

    /**
     * The {@code jda} bean is already fully connected by the time it's injected here (its
     * {@code @Bean} method blocks on {@code awaitReady()}), so JDA can start delivering gateway
     * events on its own threads immediately once the listener below is attached - potentially
     * while Spring is still mid-{@code refresh()} creating other beans (cache/AOP infrastructure
     * included). Attaching the listener only once the whole context is up avoids publishing events
     * into a not-yet-fully-initialized context.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerListener() {
        logger.info("Adding base JDA event publisher");

        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGenericEvent(@NotNull GenericEvent event) {
                publisher.publishEvent(new PayloadApplicationEvent<>(jda, new KeithEvent<>(event))); // will get auto converted to PayloadApplicationEvent
            }
        });
    }
}
