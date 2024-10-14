package com.naeayedea.keith.listener;

import com.naeayedea.keith.commands.text.ReactionCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.model.Candidate;
import com.naeayedea.keith.model.Server;
import com.naeayedea.keith.ratelimiter.CommandRateLimiter;
import com.naeayedea.keith.util.MultiMap;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class MessageReactionAddEventListener {

    private final Logger logger = LoggerFactory.getLogger(MessageReactionAddEventListener.class);

    private final ExecutorService reactionHandlingService;

    private MultiMap<String, ReactionCommand> reactionCommands;

    private final ServerManager serverManager;

    private final CandidateManager candidateManager;

    private final CommandRateLimiter rateLimiter;

    private final List<ReactionCommand> reactionCommandHandlers;

    public MessageReactionAddEventListener(@Qualifier("reactionService") ExecutorService reactionHandlingService, ServerManager serverManager, CandidateManager candidateManager, CommandRateLimiter rateLimiter, List<ReactionCommand> reactionCommandHandlers) {
        this.reactionHandlingService = reactionHandlingService;
        this.serverManager = serverManager;
        this.candidateManager = candidateManager;
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

    @EventListener(MessageReactionAddEvent.class)
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        logger.info("Received reaction, starting thread, id: {}", Thread.currentThread().threadId());
        reactionHandlingService.submit(() -> {
            logger.info("Reaction thread started, id: {}", Thread.currentThread().threadId());

            Emoji emote = event.getReaction().getEmoji();

            Member member = event.getMember();

            //ensure that event is not caused by a bot and the emote is a standard emoji
            if (member != null && !member.getUser().isBot()) {
                MessageChannel channel = event.getChannel();
                //retrieve the message which is being reacted to
                Message message = channel.retrieveMessageById(event.getMessageId()).complete();
                ReactionCommand command = reactionCommands.get(emote.getAsReactionCode());
                //ensure that this emoji has a corresponding command
                if (command != null) {

                    //join any threads automatically
                    if (channel instanceof ThreadChannel thread && !thread.isJoined()) {
                        thread.join().queue();
                    }

                    try {
                        Candidate candidate = candidateManager.getCandidate(member.getUser().getId());

                        boolean isPrivateMessage = channel instanceof PrivateChannel;

                        Server server = isPrivateMessage ? null : serverManager.getServer(event.getGuild().getId());

                        //ensure that user and server has permission to use the bot
                        if (!candidate.isBanned() && (isPrivateMessage || !server.banned())) {

                            //make sure that rate limit has not been reached
                            if (rateLimiter.userPermitted(candidate.getId())) {
                                List<MessageReaction> reactions = message.getReactions();

                                //check if any of our aliases have been fired before
                                for (MessageReaction reaction : reactions) {
                                    if (reaction.isSelf() && command.triggeredBy(reaction.getEmoji())) {
                                        return;
                                    }
                                }

                                rateLimiter.incrementOrInsertRecord(candidate.getId(), command.getCost());

                                message.addReaction(emote).queue(success -> {
                                    try {
                                        command.run(event, member.getUser());
                                    } catch (KeithPermissionException | KeithExecutionException e) {
                                        logger.error(e.getMessage(), e);

                                        message.removeReaction(emote).queue();
                                    }
                                });
                            }
                        }
                    } catch (SQLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

}
