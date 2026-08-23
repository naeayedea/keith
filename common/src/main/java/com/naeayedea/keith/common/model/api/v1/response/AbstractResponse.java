/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.common.model.api.v1.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileResponse;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Type info is needed here (rather than only on leaf types like {@link TextResponse}) because
 * {@code core} sometimes needs to carry "a response, whichever kind" through another wrapper - see
 * {@code OutboundMessageEvent} - where the concrete response type isn't known until deserialized.
 *
 * @author naeayedea
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "responseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextResponse.class, name = "text"),
    @JsonSubTypes.Type(value = TileResponse.class, name = "tile")
})
public abstract class AbstractResponse {

    private final String id;

    /**
     * Generic reaction identifiers (e.g. a unicode emoji) the platform should add to the message
     * this response is a reply to, or is about, once it's finished handling this response. Empty
     * by default - a command adds to it via {@link #addReaction(String)} when it has something to
     * say beyond its main content.
     */
    private final List<String> reactions = new ArrayList<>();

    public AbstractResponse() {
        this.id = "testId";
    }

    public String getId() {
        return id;
    }

    @NonNull
    public List<String> getReactions() {
        return List.copyOf(reactions);
    }

    /**
     * Replaces the reaction list wholesale - only needed so Jackson can round-trip this field on
     * deserialization, since {@link #getReactions()} deliberately returns an immutable copy rather
     * than the live list.
     */
    public void setReactions(@NonNull List<String> reactions) {
        this.reactions.clear();
        this.reactions.addAll(reactions);
    }

    public void addReaction(@NonNull String reaction) {
        reactions.add(reaction);
    }
}
