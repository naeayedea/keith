package com.naeayedea.keith.platform.discord.config.amqp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.naeayedea.keith.common.model.event.leaf.LeafEventRouting;
import com.naeayedea.keith.platform.discord.utils.KeithDiscordConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Every live discord replica gets its own anonymous, auto-deleting queue bound to the shared
 * {@code keith.leaf-events} exchange - this is a broadcast, not a work queue: every replica needs
 * every event for its platform so it can decide for itself whether it owns the target channel
 * (see {@link com.naeayedea.keith.platform.discord.event.LeafEventListener}). A durable named
 * queue shared across replicas would instead split events between them, which is wrong here.
 *
 * <p>A replica that's down when an event is published simply misses it - there's no backfill on
 * reconnect yet. In practice this only matters for session-state sync (a restarted replica won't
 * know about sessions that started before it came back up until they naturally end and a new one
 * starts); worth a reconciliation mechanism later, not needed to prove the pattern now.
 *
 * @author naeayedea
 */
@Configuration
public class AmqpConfig {

    @Bean
    public TopicExchange leafEventExchange() {
        return new TopicExchange(LeafEventRouting.EXCHANGE, true, false);
    }

    /**
     * Deliberately not Spring AMQP's {@code AnonymousQueue} - it declares an
     * {@code x-queue-master-locator} argument for backward compatibility with old RabbitMQ HA
     * queue behavior, and RabbitMQ 4.x now rejects that argument outright as a removed
     * deprecated feature. This gets the same "unique, exclusive, auto-deleting" shape without it.
     */
    @Bean
    public Queue leafEventQueue() {
        return QueueBuilder.nonDurable("leaf-events.discord." + UUID.randomUUID())
            .exclusive()
            .autoDelete()
            .build();
    }

    @Bean
    public Binding leafEventBinding(Queue leafEventQueue, TopicExchange leafEventExchange) {
        return BindingBuilder.bind(leafEventQueue)
            .to(leafEventExchange)
            .with(LeafEventRouting.platformBindingPattern(KeithDiscordConstants.PLATFORM_NAME));
    }

    @Bean
    public MessageConverter leafEventMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
