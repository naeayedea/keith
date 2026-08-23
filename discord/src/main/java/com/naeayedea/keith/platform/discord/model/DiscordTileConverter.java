package com.naeayedea.keith.platform.discord.model;

import com.naeayedea.keith.common.model.api.v1.response.tile.FieldTile;
import com.naeayedea.keith.common.model.api.v1.response.tile.Tile;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileContainer;
import com.naeayedea.keith.common.model.api.v1.response.tile.TextTile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Maps the generic {@link Tile} content model onto a Discord {@link MessageEmbed} - the one place
 * that knows how core's platform-agnostic response shape becomes a Discord-native one. A future
 * Slack/Teams leaf app would need its own equivalent, mapping the same {@link TileContainer} onto
 * Block Kit/Adaptive Cards instead.
 *
 * @author naeayedea
 */
@Component
public class DiscordTileConverter {

    /**
     * JDA rejects a blank embed field name - a zero-width space renders as an effectively unnamed
     * field instead.
     */
    private static final String ZERO_WIDTH_SPACE = "​";

    /**
     * @param tile the tile to render, typically a {@link TileContainer}
     * @return a built {@link MessageEmbed} equivalent to the given tile
     */
    @NonNull
    public MessageEmbed toEmbed(@NonNull Tile<?> tile) {
        EmbedBuilder builder = new EmbedBuilder();

        if (tile instanceof TileContainer container) {
            applyContainer(builder, container);
        } else if (tile instanceof TextTile textTile) {
            builder.setDescription(textTile.getContent());
        }

        return builder.build();
    }

    private void applyContainer(
        @NonNull EmbedBuilder builder,
        @NonNull TileContainer container
    ) {
        builder.setTitle(container.getTitle(), container.getUrl());
        builder.setDescription(container.getDescription());

        //EmbedBuilder.setColor takes a primitive int - only call it when there's a color to unbox
        if (container.getColor() != null) {
            builder.setColor(container.getColor());
        }

        builder.setImage(container.getImageUrl());
        builder.setThumbnail(container.getThumbnailUrl());

        if (container.getAuthor() != null) {
            TileContainer.Author author = container.getAuthor();

            builder.setAuthor(author.name(), author.url(), author.iconUrl());
        }

        if (container.getFooter() != null) {
            TileContainer.Footer footer = container.getFooter();

            builder.setFooter(footer.text(), footer.iconUrl());
        }

        if (container.getTimestamp() != null) {
            builder.setTimestamp(OffsetDateTime.ofInstant(container.getTimestamp(), ZoneOffset.UTC));
        }

        for (Tile<?> child : container.getTiles()) {
            if (child instanceof FieldTile field) {
                builder.addField(field.getName(), field.getContent(), field.isInline());
            } else if (child instanceof TextTile text) {
                builder.addField(ZERO_WIDTH_SPACE, text.getContent(), false);
            }
        }
    }
}
