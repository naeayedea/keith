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
import com.naeayedea.keith.common.model.api.v1.response.AbstractResponse;
import org.jspecify.annotations.NonNull;

/**
 * @author naeayedea
 */
public class TileResponse extends AbstractResponse {

    /**
     * The tile this response wraps.
     */
    private final Tile<?> tile;

    @JsonCreator
    public TileResponse(@JsonProperty("tile") @NonNull Tile<?> tile) {
        this.tile = tile;
    }

    @NonNull
    public Tile<?> getTile() {
        return tile;
    }

    @NonNull
    public static TileResponse of(@NonNull Tile<?> tile) {
        return new TileResponse(tile);
    }
}
