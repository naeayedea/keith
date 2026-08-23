package com.naeayedea.keith.core.channel;

import org.jspecify.annotations.NonNull;

/**
 * Identifies a channel a channel-scoped session (a game, a chat pairing) lives in. Used as a map
 * key throughout the {@code channel} package rather than a raw string, since a session is scoped
 * to a specific channel on a specific platform, not just a channel id in isolation.
 *
 * @param platform the platform the channel belongs to, e.g. {@code "discord"}
 * @param channelId the channel id, as that platform identifies it
 * @author naeayedea
 */
public record ChannelRef(@NonNull String platform, @NonNull String channelId) {
}
