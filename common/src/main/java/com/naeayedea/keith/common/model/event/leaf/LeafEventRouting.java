package com.naeayedea.keith.common.model.event.leaf;

import org.jspecify.annotations.NonNull;

/**
 * Naming shared between {@code core} (publisher) and every leaf app (consumer) so the exchange and
 * routing key scheme only lives in one place. Routing key is {@code <platform>.<eventType>} - a
 * leaf app binds a queue to {@code <platform>.#} to receive every event type for its platform.
 *
 * @author naeayedea
 */
public final class LeafEventRouting {

    /**
     * The single topic exchange every {@link LeafEvent} is published to, regardless of type or
     * target platform - routing is entirely down to the routing key and each consumer's binding.
     */
    public static final String EXCHANGE = "keith.leaf-events";

    private LeafEventRouting() {
    }

    @NonNull
    public static String routingKey(@NonNull LeafEvent event) {
        return routingKey(event.getPlatform(), event.getClass().getSimpleName());
    }

    @NonNull
    public static String routingKey(@NonNull String platform, @NonNull String eventType) {
        return platform + "." + eventType;
    }

    @NonNull
    public static String platformBindingPattern(@NonNull String platform) {
        return platform + ".#";
    }
}
