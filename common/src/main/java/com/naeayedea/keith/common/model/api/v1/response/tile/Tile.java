/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.common.model.api.v1.response.tile;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A single piece of generic structured content. This is the common denominator that platform
 * transformers map to their own rich-content format (e.g. a Discord {@code MessageEmbed}, a Slack
 * Block Kit block, a Teams Adaptive Card element) and that {@code core}'s HTTP API also returns
 * directly to non-chat callers.
 *
 * <p>Deserializing a {@code Tile} needs a type discriminator since it's an interface - every
 * implementation must be registered in the {@link JsonSubTypes} list below.
 *
 * @param <T> the shape of this tile's content
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tileType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextTile.class, name = "text"),
    @JsonSubTypes.Type(value = FieldTile.class, name = "field"),
    @JsonSubTypes.Type(value = TileContainer.class, name = "container")
})
public interface Tile<T> {

    T getContent();
}
