package com.naeayedea.keith.platform.discord.event;

import com.naeayedea.keith.common.model.event.leaf.SessionStateEvent;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A local, best-effort cache of which channels core says currently have an active channel-scoped
 * session (a game, a chat relay), kept in sync via {@link SessionStateEvent}. Its entire purpose
 * is to avoid an HTTP round trip to core for every single plain chat message - only messages in a
 * channel this registry already knows is active get forwarded for evaluation.
 *
 * @author naeayedea
 */
@Component
public class ActiveSessionRegistry {

    private final Set<String> activeChannelIds = ConcurrentHashMap.newKeySet();

    public boolean isActive(@NonNull String channelId) {
        return activeChannelIds.contains(channelId);
    }

    public void markActive(@NonNull String channelId) {
        activeChannelIds.add(channelId);
    }

    public void markInactive(@NonNull String channelId) {
        activeChannelIds.remove(channelId);
    }
}
