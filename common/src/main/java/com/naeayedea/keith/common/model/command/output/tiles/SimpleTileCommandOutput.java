package com.naeayedea.keith.common.model.command.output.tiles;

import com.naeayedea.keith.common.model.api.v1.response.tile.Tile;

public class SimpleTileCommandOutput implements TileCommandOutput {

    private final Tile<?> tile;

    public SimpleTileCommandOutput(Tile<?> tile) {
        this.tile = tile;
    }

    @Override
    public Tile<?> getTile() {
        return tile;
    }
}
