package com.naeayedea.keith.core.config.amqp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.naeayedea.keith.common.model.event.leaf.LeafEventRouting;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the single topic exchange every {@link com.naeayedea.keith.common.model.event.leaf.LeafEvent}
 * is published to - see {@link LeafEventRouting} for the routing key scheme leaf apps bind their
 * own queues against.
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
     * Spring Boot's autoconfigured {@code RabbitTemplate} picks up this bean automatically since
     * it's the only {@link MessageConverter} in the context - needed so events serialize as JSON
     * (with the polymorphic type info the various {@code LeafEvent}/{@code AbstractResponse}
     * subtypes rely on) rather than Java's default binary format.
     */
    @Bean
    public MessageConverter leafEventMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
