package com.naeayedea.keith.listener;


import com.naeayedea.keith.commands.IReactionCommand;
import com.naeayedea.keith.commands.generic.Pin;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.managers.UserManager;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Component
public class MessageReactionAddEventListener {

    private final Logger logger = LoggerFactory.getLogger(MessageReactionAddEventListener.class);

    private final ExecutorService reactionHandlingService;

    private Map<String, IReactionCommand> reactionCommands;

    private final ServerManager serverManager;

    private final UserManager userManager;

    private final CommandRateLimiter rateLimiter;

    public MessageReactionAddEventListener(@Qualifier("reactionService") ExecutorService reactionHandlingService, ServerManager serverManager, UserManager userManager, CommandRateLimiter rateLimiter) {
        this.reactionHandlingService = reactionHandlingService;
        this.serverManager = serverManager;
        this.userManager = userManager;
        this.rateLimiter = rateLimiter;
    }

    @PostConstruct
    private void init() {
        this.reactionCommands = new HashMap<>();

        //reaction commands
        reactionCommands = new MultiMap<>();
        reactionCommands.put("📌", new Pin());

        logger.info("Loaded {} reaction commands.", reactionCommands.size());

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
                IReactionCommand command = reactionCommands.get(emote.getAsReactionCode());
                //ensure that this emoji has a corresponding command
                if (command != null) {

                    //join any threads automatically
                    if (channel instanceof ThreadChannel thread && !thread.isJoined()) {
                        thread.join().queue();
                    }
                    UserManager.User user = userManager.getUser(member.getUser().getId());

                    boolean isPrivateMessage = channel instanceof PrivateChannel;

                    ServerManager.Server server = isPrivateMessage ? null : serverManager.getServer(event.getGuild().getId());

                    //ensure that user and server has permission to use the bot
                    if (!user.isBanned() && (isPrivateMessage || !server.isBanned())) {

                        //make sure that rate limit has not been reached
                        if (rateLimiter.userPermitted(user.getId())) {
                            List<MessageReaction> reactions = message.getReactions();
                            for (MessageReaction reaction : reactions) {
                                if (reaction.getEmoji().equals(emote)) {
                                    if (reaction.hasCount() && reaction.getCount() < 2) {
                                        rateLimiter.incrementOrInsertRecord(user.getId(), 1);
                                        message.addReaction(emote).queue(e -> command.run(event, member.getUser()));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

}
