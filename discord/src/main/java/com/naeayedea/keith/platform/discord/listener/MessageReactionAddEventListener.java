package com.naeayedea.keith.platform.discord.listener;

import com.naeayedea.keith.core.model.event.KeithEvent;
import com.naeayedea.keith.core.model.user.KeithUser;
import com.naeayedea.keith.platform.discord.lib.command.ReactionCommand;
import com.naeayedea.keith.core.exception.KeithException;
import com.naeayedea.keith.core.managers.KeithUserManager;
import com.naeayedea.keith.core.managers.ServerManager;
import com.naeayedea.keith.core.model.user.BasicKeithUser;
import com.naeayedea.keith.core.model.server.BasicKeithServer;
import com.naeayedea.keith.core.ratelimiter.CommandRateLimiter;
import com.naeayedea.keith.core.util.MultiMap;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class MessageReactionAddEventListener extends AbstractUserPermittingDiscordEventListener<MessageReactionAddEvent> {

    private final Logger logger = LoggerFactory.getLogger(MessageReactionAddEventListener.class);

    private MultiMap<String, ReactionCommand> reactionCommands;

    private final ServerManager serverManager;

    private final KeithUserManager keithUserManager;

    private final CommandRateLimiter rateLimiter;

    private final List<ReactionCommand> reactionCommandHandlers;

    public MessageReactionAddEventListener(@Qualifier("reactionService") ExecutorService reactionHandlingService, ServerManager serverManager, KeithUserManager keithUserManager, CommandRateLimiter rateLimiter, List<ReactionCommand> reactionCommandHandlers) {
        this.serverManager = serverManager;
        this.keithUserManager = keithUserManager;
        this.rateLimiter = rateLimiter;
        this.reactionCommandHandlers = reactionCommandHandlers;
    }

    @PostConstruct
    private void init() {
        this.reactionCommands = new MultiMap<>();

        for (ReactionCommand command : reactionCommandHandlers) {
            reactionCommands.putAll(command.getReactionTriggers().stream().map(Emoji::getAsReactionCode).toList(), command);
        }

        logger.info("Loaded {} reaction aliases.", reactionCommands.size());

    }

    @EventListener
    @Async
    @Override
    public void onEvent(KeithEvent<MessageReactionAddEvent> eventSource) {
        super.onEvent(eventSource);
    }

    @Override
    protected KeithUser getUser(MessageReactionAddEvent event) {
        return keithUserManager.getUser(event.getUserId());
    }

    @Override
    protected boolean isBot(MessageReactionAddEvent event) {
        return event.getUser() != null && (event.getUser().isBot() || event.getUser().isSystem());
    }

    @Override
    public void onPermitted(MessageReactionAddEvent event) throws Exception {
        Emoji emote = event.getReaction().getEmoji();

        Member member = event.getMember();

        //ensure that event is not caused by a bot
        if (member == null || member.getUser().isBot()) {
            return;
        }

        MessageChannel channel = event.getChannel();

        //retrieve the message which is being reacted to and the reaction itself
        Message message = channel.retrieveMessageById(event.getMessageId()).complete();

        ReactionCommand command = reactionCommands.get(emote.getAsReactionCode());

        //ensure that this emoji has a corresponding command
        if (command == null) {
            return;
        }

        boolean isPrivateMessage = channel instanceof PrivateChannel;

        //if this is a private message then ensure we can run the command
        if (isPrivateMessage && !command.isPrivateMessageCompatible()) {
            return;
        }

        //join any threads automatically
        if (channel instanceof ThreadChannel thread && !thread.isJoined()) {
            thread.join().complete();
        }

        BasicKeithUser keithUser = keithUserManager.getUser(member.getUser().getId());

        if (keithUser.isBanned()) {
            return;
        }

        BasicKeithServer keithServer = isPrivateMessage ? null : serverManager.getServer(event.getGuild().getId());

        //if not private message, ensure server has permission to use the bot
        if (!isPrivateMessage && keithServer.isBanned()) {
            return;
        }

        //make sure that rate limit has not been reached
        if (rateLimiter.userPermitted(keithUser.getId())) {
            List<MessageReaction> reactions = message.getReactions();

            //check if any of our aliases have been fired before
            for (MessageReaction reaction : reactions) {
                if (reaction.isSelf() && command.triggeredBy(reaction.getEmoji())) {
                    return;
                }
            }

            rateLimiter.incrementOrInsertRecord(keithUser.getId(), command.getCost());

            message.addReaction(emote).queue(success -> {
                try {
                    command.run(event, member.getUser());
                } catch (KeithException e) {
                    logger.error(e.getMessage(), e);

                    message.removeReaction(emote).queue();
                }
            });
        }
    }
}
