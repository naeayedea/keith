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
 * A freeform block of plain text, with no name/value structure. For a named field (e.g. an embed
 * field), use {@link FieldTile} instead.
 *
 * @author naeayedea
 */
public class TextTile implements Tile<String> {

    /**
     * The text this tile carries.
     */
    private final String content;

    @JsonCreator
    public TextTile(@JsonProperty("content") @NonNull String content) {
        this.content = content;
    }

    @Override
    @NonNull
    public String getContent() {
        return content;
    }
}
