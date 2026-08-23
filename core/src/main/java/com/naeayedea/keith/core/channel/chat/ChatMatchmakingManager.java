package com.naeayedea.keith.core.channel.chat;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.event.leaf.OutboundMessageEvent;
import com.naeayedea.keith.core.channel.ChannelRef;
import com.naeayedea.keith.core.event.LeafEventPublisher;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The matchmaking queue for inter-server chat: the first channel to ask for a partner waits here
 * until a second one asks, at which point {@link ChatSessionManager} pairs them. There's no push
 * back to the first channel mid-request - it finds out it's matched the same way every other
 * cross-channel notification works, via {@link OutboundMessageEvent}.
 *
 * @author naeayedea
 */
@Component
public class ChatMatchmakingManager {

    private final Map<ChannelRef, Long> queue = new LinkedHashMap<>();

    private final ChatSessionManager sessionManager;

    private final LeafEventPublisher publisher;

    public ChatMatchmakingManager(
        ChatSessionManager sessionManager,
        LeafEventPublisher publisher
    ) {
        this.sessionManager = sessionManager;
        this.publisher = publisher;
    }

    @NonNull
    public synchronized TextResponse start(@NonNull ChannelRef ref) {
        if (sessionManager.hasSession(ref)) {
            return TextResponse.simple("This channel already has an active chat - use chat close to end it first.");
        }

        if (queue.containsKey(ref)) {
            return TextResponse.simple("Already searching for a chat partner - use chat cancel to stop.");
        }

        ChannelRef partner = queue.keySet().stream().findFirst().orElse(null);

        if (partner == null) {
            queue.put(ref, System.currentTimeMillis());

            return TextResponse.simple("Searching for a chat partner (this could take a while)...");
        }

        queue.remove(partner);

        sessionManager.pair(ref, partner);

        String matchedMessage = "Connected! Say hi - messages you send here will be relayed. Use chat close to end the chat.";

        publisher.publish(new OutboundMessageEvent(partner.platform(), partner.channelId(), TextResponse.simple(matchedMessage)));

        return TextResponse.simple(matchedMessage);
    }

    public synchronized boolean cancel(@NonNull ChannelRef ref) {
        return queue.remove(ref) != null;
    }

    public synchronized boolean isSearching(@NonNull ChannelRef ref) {
        return queue.containsKey(ref);
    }
}
