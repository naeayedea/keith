package com.naeayedea.keith.platform.discord.command.reaction.pin;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.command.CommandInformationProvider;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import com.naeayedea.keith.platform.discord.client.CoreApiClient;
import com.naeayedea.keith.platform.discord.command.lib.ReactionCommandHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

/**
 * Pin, the first reaction-triggered command: reacting to a message with 📌 calls core's
 * {@code /api/v1/command/reaction/pin} the same way {@code PingCommandHandler}/
 * {@code HelpCommandHandler} call theirs, then applies whatever reactions the response asks for
 * to the message that was reacted to.
 *
 * <p>{@link com.naeayedea.keith.platform.discord.listener.message.MessageReactionAddEventListener}
 * already echoes the trigger emoji back onto the message itself before running the command - the
 * confirmation reaction core sends back here is deliberately a different emoji, so the two are
 * distinguishable in a real Discord channel.
 *
 * @author naeayedea
 */
@Component
public class PinCommandHandler implements ReactionCommandHandler, CommandInformationProvider {

    private static final Logger logger = LoggerFactory.getLogger(PinCommandHandler.class);

    private static final String PIN_PATH = "/api/v1/command/reaction/pin";

    private static final Emoji TRIGGER_EMOJI = Emoji.fromUnicode("📌");

    private final String internalName;

    private final String nameKey;

    private final String aliasKey;

    private final CoreApiClient coreApiClient;

    public PinCommandHandler(
        @Value("${keith.commands.pin.internal-name}") String internalName,
        @Value("${keith.commands.pin.name-key}") String nameKey,
        @Value("${keith.commands.pin.alias-key}") String aliasKey,
        CoreApiClient coreApiClient
    ) {
        this.internalName = internalName;
        this.nameKey = nameKey;
        this.aliasKey = aliasKey;
        this.coreApiClient = coreApiClient;
    }

    @Override
    @NonNull
    public List<Emoji> getReactionTriggers() {
        return List.of(TRIGGER_EMOJI);
    }

    @Override
    public boolean triggeredBy(@NonNull Emoji emoji) {
        return TRIGGER_EMOJI.getAsReactionCode().equals(emoji.getAsReactionCode());
    }

    @Override
    public void run(
        @NonNull MessageReactionAddEvent event,
        @NonNull User user
    ) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        try {
            TextResponse response = coreApiClient.getAsUser(
                PIN_PATH,
                user.getId(),
                Map.of("reactionKey", TRIGGER_EMOJI.getAsReactionCode()),
                TextResponse.class
            );

            MessageChannel channel = event.getChannel();
            Message message = channel.retrieveMessageById(event.getMessageId()).complete();

            for (String reaction : response.getReactions()) {
                message.addReaction(Emoji.fromUnicode(reaction)).queue();
            }
        } catch (HttpClientErrorException e) {
            logger.error("Pin command failed calling core", e);

            throw new KeithExecutionException(e);
        }
    }

    @Override
    @NonNull
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

    @Override
    public int getTimeOut() {
        return 10;
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return true;
    }

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    @NonNull
    public String getInternalName() {
        return internalName;
    }

    @Override
    @NonNull
    public String getAliasTranslationKey() {
        return aliasKey;
    }

    @Override
    @NonNull
    public String getNameTranslationKey() {
        return nameKey;
    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
