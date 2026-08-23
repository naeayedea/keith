package com.naeayedea.keith.core.event;

import com.naeayedea.keith.common.model.event.leaf.LeafEvent;
import com.naeayedea.keith.common.model.event.leaf.LeafEventRouting;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * The one place core publishes a {@link LeafEvent} from - every leaf-app-owned side effect that
 * doesn't fit in the response to the request currently being handled goes through here.
 *
 * @author naeayedea
 */
@Component
public class LeafEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(LeafEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public LeafEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(@NonNull LeafEvent event) {
        String routingKey = LeafEventRouting.routingKey(event);

        logger.debug("Publishing {} to {}", event.getClass().getSimpleName(), routingKey);

        rabbitTemplate.convertAndSend(LeafEventRouting.EXCHANGE, routingKey, event);
    }
}
