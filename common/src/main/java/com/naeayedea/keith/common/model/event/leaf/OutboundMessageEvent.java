package com.naeayedea.keith.common.model.event.leaf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naeayedea.keith.common.model.api.v1.response.AbstractResponse;
import org.jspecify.annotations.NonNull;

/**
 * A response {@code core} needs delivered to a channel with no in-flight HTTP request to carry it
 * back through - a chat relay message going to the *other* channel in the pairing, a match
 * notification, a session timing out.
 *
 * @param platform the platform the channel belongs to, e.g. {@code "discord"}
 * @param channelId the channel to deliver the response to
 * @param response what to deliver - the same {@link AbstractResponse} shape a leaf app already
 *                  knows how to render from any synchronous command call
 * @author naeayedea
 */
public record OutboundMessageEvent(
    @NonNull String platform,
    @NonNull String channelId,
    @NonNull AbstractResponse response
) implements LeafEvent {

    @JsonCreator
    public OutboundMessageEvent(
        @JsonProperty("platform") @NonNull String platform,
        @JsonProperty("channelId") @NonNull String channelId,
        @JsonProperty("response") @NonNull AbstractResponse response
    ) {
        this.platform = platform;
        this.channelId = channelId;
        this.response = response;
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
