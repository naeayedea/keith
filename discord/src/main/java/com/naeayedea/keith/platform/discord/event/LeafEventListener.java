package com.naeayedea.keith.platform.discord.event;

import com.naeayedea.keith.common.model.api.v1.response.AbstractResponse;
import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileResponse;
import com.naeayedea.keith.common.model.event.leaf.LeafEvent;
import com.naeayedea.keith.common.model.event.leaf.OutboundMessageEvent;
import com.naeayedea.keith.common.model.event.leaf.SessionStateEvent;
import com.naeayedea.keith.platform.discord.model.DiscordTileConverter;
import com.naeayedea.keith.platform.discord.utils.Utilities;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Receives every {@link LeafEvent} core publishes for this platform (see
 * {@link com.naeayedea.keith.platform.discord.config.amqp.AmqpConfig}) and either updates the
 * local {@link ActiveSessionRegistry} or delivers a pushed message via JDA - the discord-side half
 * of the core-to-leaf push path that plain HTTP request/response can't cover.
 *
 * @author naeayedea
 */
@Component
public class LeafEventListener {

    private static final Logger logger = LoggerFactory.getLogger(LeafEventListener.class);

    private final ActiveSessionRegistry activeSessionRegistry;

    private final DiscordTileConverter tileConverter;

    public LeafEventListener(
        ActiveSessionRegistry activeSessionRegistry,
        DiscordTileConverter tileConverter
    ) {
        this.activeSessionRegistry = activeSessionRegistry;
        this.tileConverter = tileConverter;
    }

    /**
     * Deliberately never lets an exception escape to Spring AMQP - its default behavior on an
     * unhandled listener exception is to reject-and-requeue, which for a single bad/unprocessable
     * message becomes an infinite redelivery loop (the same message forever, at full CPU) rather
     * than a handful of retries. A malformed or momentarily-unhandleable event is logged and
     * dropped instead - there's no dead-letter queue to inspect it in later, which is worth adding
     * if this turns out to matter in practice.
     */
    @RabbitListener(queues = "#{leafEventQueue.name}")
    public void onLeafEvent(@NonNull LeafEvent event) {
        try {
            switch (event) {
                case SessionStateEvent sessionStateEvent -> onSessionStateEvent(sessionStateEvent);
                case OutboundMessageEvent outboundMessageEvent -> onOutboundMessageEvent(outboundMessageEvent);
                default -> logger.warn("No handler for leaf event type {}", event.getClass());
            }
        } catch (Exception e) {
            logger.error("Failed to handle leaf event {}, dropping it", event, e);
        }
    }

    private void onSessionStateEvent(@NonNull SessionStateEvent event) {
        if (event.active()) {
            activeSessionRegistry.markActive(event.channelId());
        } else {
            activeSessionRegistry.markInactive(event.channelId());
        }
    }

    private void onOutboundMessageEvent(@NonNull OutboundMessageEvent event) {
        MessageChannel channel = Utilities.getMessageChannelById(event.channelId());

        if (channel == null) {
            logger.warn("Received an outbound message for unknown channel {}", event.channelId());

            return;
        }

        AbstractResponse response = event.response();

        if (response instanceof TileResponse tileResponse) {
            channel.sendMessageEmbeds(tileConverter.toEmbed(tileResponse.getTile()))
                .queue(sent -> applyReactions(sent, response));
        } else if (response instanceof TextResponse textResponse && !textResponse.getText().isBlank()) {
            channel.sendMessage(textResponse.getText()).queue(sent -> applyReactions(sent, response));
        }
    }

    private void applyReactions(@NonNull Message message, @NonNull AbstractResponse response) {
        for (String reaction : response.getReactions()) {
            message.addReaction(Emoji.fromUnicode(reaction)).queue();
        }
    }
}
