/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.common.model.api.v1.response.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

/**
 * A named name/value field, the generic equivalent of a Discord embed field. {@code inline}
 * mirrors the same concept across chat platforms that support it (Discord embed fields, Slack
 * section fields) and is safe to ignore on platforms that don't.
 *
 * @author naeayedea
 */
public class FieldTile implements Tile<String> {

    /**
     * The field's label.
     */
    private final String name;

    /**
     * The field's value, exposed as this tile's {@link #getContent() content}.
     */
    private final String value;

    /**
     * Whether the platform should lay this field out inline with its neighbours, where supported.
     */
    private final boolean inline;

    @JsonCreator
    public FieldTile(
        @JsonProperty("name") @NonNull String name,
        @JsonProperty("content") @NonNull String value,
        @JsonProperty("inline") boolean inline
    ) {
        this.name = name;
        this.value = value;
        this.inline = inline;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public String getContent() {
        return value;
    }

    public boolean isInline() {
        return inline;
    }
}
