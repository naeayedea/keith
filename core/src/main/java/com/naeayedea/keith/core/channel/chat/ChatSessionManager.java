package com.naeayedea.keith.core.channel.chat;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileContainer;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileResponse;
import com.naeayedea.keith.common.model.event.leaf.OutboundMessageEvent;
import com.naeayedea.keith.common.model.event.leaf.SessionStateEvent;
import com.naeayedea.keith.core.channel.ChannelRef;
import com.naeayedea.keith.core.event.LeafEventPublisher;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Active inter-server chat pairings: two channels relaying messages to each other until either
 * side closes the connection. Unlike a guess game, there's no timeout - this stays open for as
 * long as the users involved want it to.
 *
 * @author naeayedea
 */
@Component
public class ChatSessionManager {

    private final Map<ChannelRef, ChannelRef> pairings = new ConcurrentHashMap<>();

    private final LeafEventPublisher publisher;

    public ChatSessionManager(LeafEventPublisher publisher) {
        this.publisher = publisher;
    }

    public boolean hasSession(@NonNull ChannelRef ref) {
        return pairings.containsKey(ref);
    }

    public void pair(@NonNull ChannelRef a, @NonNull ChannelRef b) {
        pairings.put(a, b);
        pairings.put(b, a);

        publisher.publish(new SessionStateEvent(a.platform(), a.channelId(), true));
        publisher.publish(new SessionStateEvent(b.platform(), b.channelId(), true));
    }

    /**
     * Relays a message to the sender's chat partner. There's deliberately no return value for the
     * sender's own channel - the relay always goes to the *other* side, delivered via
     * {@link OutboundMessageEvent} since it's never the channel the triggering HTTP request came
     * from.
     */
    public void relay(
        @NonNull ChannelRef sender,
        @NonNull String authorName,
        @NonNull String content
    ) {
        ChannelRef partner = pairings.get(sender);

        if (partner == null) {
            return;
        }

        TileResponse response = TileResponse.of(
            TileContainer.builder()
                .author(authorName, null, null)
                .description(content)
                .build()
        );

        publisher.publish(new OutboundMessageEvent(partner.platform(), partner.channelId(), response));
    }

    @NonNull
    public TextResponse close(@NonNull ChannelRef ref) {
        ChannelRef partner = pairings.remove(ref);

        if (partner == null) {
            return TextResponse.simple("No active chat to close.");
        }

        pairings.remove(partner);

        publisher.publish(new SessionStateEvent(ref.platform(), ref.channelId(), false));
        publisher.publish(new SessionStateEvent(partner.platform(), partner.channelId(), false));

        publisher.publish(new OutboundMessageEvent(
            partner.platform(),
            partner.channelId(),
            TextResponse.simple("Chat connection closed by the other side.")
        ));

        return TextResponse.simple("Chat connection closed.");
    }
}
