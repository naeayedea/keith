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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

/**
 * A collection of {@link Tile Tiles} with the fixed metadata slots common to rich chat messages
 * (Discord embeds, Slack Block Kit, Teams Adaptive Cards): title/description/color/author/footer/
 * image/thumbnail, plus an ordered list of child tiles (typically {@link FieldTile FieldTiles}).
 * This is the generic "embed" of the platform - each leaf app's {@code ResponseTransformer} maps
 * it onto that platform's native rich-content type.
 *
 * @author naeayedea
 */
public class TileContainer implements Tile<List<Tile<?>>> {

    private final List<Tile<?>> tiles;

    @Nullable
    private final String title;

    @Nullable
    private final String description;

    @Nullable
    private final String url;

    @Nullable
    private final Integer color;

    @Nullable
    private final Author author;

    @Nullable
    private final Footer footer;

    @Nullable
    private final String imageUrl;

    @Nullable
    private final String thumbnailUrl;

    @Nullable
    private final Instant timestamp;

    @JsonCreator
    public TileContainer(
        @JsonProperty("tiles") List<Tile<?>> tiles,
        @JsonProperty("title") @Nullable String title,
        @JsonProperty("description") @Nullable String description,
        @JsonProperty("url") @Nullable String url,
        @JsonProperty("color") @Nullable Integer color,
        @JsonProperty("author") @Nullable Author author,
        @JsonProperty("footer") @Nullable Footer footer,
        @JsonProperty("imageUrl") @Nullable String imageUrl,
        @JsonProperty("thumbnailUrl") @Nullable String thumbnailUrl,
        @JsonProperty("timestamp") @Nullable Instant timestamp
    ) {
        this.tiles = tiles;
        this.title = title;
        this.description = description;
        this.url = url;
        this.color = color;
        this.author = author;
        this.footer = footer;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.timestamp = timestamp;
    }

    public List<Tile<?>> getTiles() {
        return tiles;
    }

    /**
     * Same data as {@link #getTiles()} - required to satisfy {@link Tile#getContent()}, not a
     * second copy on the wire.
     */
    @Override
    @JsonIgnore
    public List<Tile<?>> getContent() {
        return tiles;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public Integer getColor() {
        return color;
    }

    @Nullable
    public Author getAuthor() {
        return author;
    }

    @Nullable
    public Footer getFooter() {
        return footer;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    @Nullable
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Nullable
    public Instant getTimestamp() {
        return timestamp;
    }

    public record Author(String name, @Nullable String url, @Nullable String iconUrl) {
    }

    public record Footer(String text, @Nullable String iconUrl) {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final List<Tile<?>> tiles = new java.util.ArrayList<>();

        private String title;

        private String description;

        private String url;

        private Integer color;

        private Author author;

        private Footer footer;

        private String imageUrl;

        private String thumbnailUrl;

        private Instant timestamp;

        private Builder() {}

        public Builder addTile(Tile<?> tile) {
            this.tiles.add(tile);

            return this;
        }

        public Builder addField(String name, String value, boolean inline) {
            this.tiles.add(new FieldTile(name, value, inline));

            return this;
        }

        public Builder title(String title) {
            this.title = title;

            return this;
        }

        public Builder description(String description) {
            this.description = description;

            return this;
        }

        public Builder url(String url) {
            this.url = url;

            return this;
        }

        public Builder color(Integer color) {
            this.color = color;

            return this;
        }

        public Builder author(String name, @Nullable String url, @Nullable String iconUrl) {
            this.author = new Author(name, url, iconUrl);

            return this;
        }

        public Builder footer(String text, @Nullable String iconUrl) {
            this.footer = new Footer(text, iconUrl);

            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;

            return this;
        }

        public Builder thumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;

            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;

            return this;
        }

        public TileContainer build() {
            return new TileContainer(List.copyOf(tiles), title, description, url, color, author, footer, imageUrl, thumbnailUrl, timestamp);
        }
    }
}
