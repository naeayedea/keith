package com.naeayedea.keith.common.model.event.leaf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

/**
 * A channel-scoped session (a game, a chat relay pairing, ...) started or ended. Leaf apps use
 * this to maintain a local cache of which channels currently have an active session, so a plain
 * chat message doesn't need an HTTP round trip to core just to find out it isn't part of one.
 *
 * @param platform the platform the channel belongs to, e.g. {@code "discord"}
 * @param channelId the channel whose session state changed
 * @param active {@code true} if a session just started, {@code false} if one just ended
 * @author naeayedea
 */
public record SessionStateEvent(
    @NonNull String platform,
    @NonNull String channelId,
    boolean active
) implements LeafEvent {

    @JsonCreator
    public SessionStateEvent(
        @JsonProperty("platform") @NonNull String platform,
        @JsonProperty("channelId") @NonNull String channelId,
        @JsonProperty("active") boolean active
    ) {
        this.platform = platform;
        this.channelId = channelId;
        this.active = active;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }
}
