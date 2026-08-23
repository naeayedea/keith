package com.naeayedea.keith.common.model.event.leaf;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Something {@code core} needs to push to a leaf app with no in-flight HTTP request to carry it -
 * a channel's session starting/ending, or a message to deliver somewhere. Published to a
 * platform-scoped topic (routing key {@code <platform>.<type>}) that every replica of that
 * platform's leaf app subscribes to; each replica decides for itself whether it owns the target
 * channel and should act on the event, since core has no registry of which replica owns what.
 *
 * @author naeayedea
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SessionStateEvent.class, name = "sessionState"),
    @JsonSubTypes.Type(value = OutboundMessageEvent.class, name = "outboundMessage")
})
public interface LeafEvent {

    String getPlatform();

    String getChannelId();
}
