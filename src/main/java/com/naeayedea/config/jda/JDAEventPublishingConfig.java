package com.naeayedea.config.jda;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class JDAEventPublishingConfig {

    Logger logger = LoggerFactory.getLogger(JDAEventPublishingConfig.class);

    public JDAEventPublishingConfig(ApplicationEventPublisher publisher, JDA jda) {
        logger.info("Adding base JDA event publisher");

        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGenericEvent(@NotNull GenericEvent event) {
                logger.trace(event.toString());

                publisher.publishEvent(new PayloadApplicationEvent<>(jda, event)); // will get auto converted to PayloadApplicationEvent
            }
        });
    }
}
