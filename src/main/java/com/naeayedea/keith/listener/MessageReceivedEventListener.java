package com.naeayedea.keith.listener;

import com.naeayedea.keith.commands.impl.text.TextCommand;
import com.naeayedea.keith.commands.impl.text.admin.AdminTextCommandPortal;
import com.naeayedea.keith.commands.impl.text.channelCommandDrivers.ChannelCommandDriver;
import com.naeayedea.keith.commands.impl.text.generic.AbstractUserTextCommand;
import com.naeayedea.keith.commands.impl.text.info.AbstractInfoTextCommand;
import com.naeayedea.keith.commands.impl.text.info.HelpTextCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.managers.ChannelCommandManager;
import com.naeayedea.keith.managers.ServerChatManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.model.Candidate;
import com.naeayedea.keith.model.Server;
import com.naeayedea.keith.ratelimiter.CommandRateLimiter;
import com.naeayedea.keith.util.MultiMap;
import com.naeayedea.keith.util.Utilities;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class MessageReceivedEventListener {

    private final Logger logger = LoggerFactory.getLogger(MessageReceivedEventListener.class);

    private MultiMap<String, TextCommand> commands;

    private final ExecutorService messageService;

    private final ExecutorService commandService;

    private final CandidateManager candidateManager;

    private final ServerManager serverManager;

    private final ChannelCommandManager channelCommandManager;

    private final ServerChatManager chatManager;

    private final CommandRateLimiter rateLimiter;

    private final AdminTextCommandPortal adminTextCommandPortal;

    private final List<TextCommand> textCommands;

    private final HelpTextCommand baseHelpTextCommand;

    public MessageReceivedEventListener(@Qualifier("messageService") ExecutorService messageService, @Qualifier("commandService") ExecutorService commandService, CandidateManager candidateManager, ServerManager serverManager, ChannelCommandManager channelCommandManager, ServerChatManager chatManager, CommandRateLimiter rateLimiter, List<AbstractInfoTextCommand> infoCommands, List<AbstractUserTextCommand> userCommands, HelpTextCommand baseHelpTextCommand, AdminTextCommandPortal adminTextCommandPortal) {
        this.messageService = messageService;
        this.candidateManager = candidateManager;
        this.serverManager = serverManager;
        this.channelCommandManager = channelCommandManager;
        this.chatManager = chatManager;
        this.commandService = commandService;
        this.rateLimiter = rateLimiter;
        this.adminTextCommandPortal = adminTextCommandPortal;

        this.textCommands = new ArrayList<>(userCommands.size() + infoCommands.size());
        this.baseHelpTextCommand = baseHelpTextCommand;

        this.textCommands.addAll(infoCommands);
        this.textCommands.addAll(userCommands);
    }

    @PostConstruct
    private void initialiseCommands() {
        logger.info("Initializing base command map.");

        commands = new MultiMap<>();

        Utilities.populateCommandMap(commands, textCommands, List.of(baseHelpTextCommand.getDefaultName()));

        commands.putAll(baseHelpTextCommand.getAliases(), baseHelpTextCommand);
        commands.putAll(adminTextCommandPortal.getAliases(), adminTextCommandPortal);

        logger.info("Loaded {} base message command aliases", commands.size());
    }

    @EventListener(MessageReceivedEvent.class)
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        messageService.submit(() -> {
            MessageChannel channel = event.getChannel();
            try {
                Message message = event.getMessage();
                //automatically join any threads that are created so that bot feels easy to use in threads
                if (channel instanceof ThreadChannel thread && !thread.isJoined()) {
                    thread.join().queue();
                }

                String messageContent = message.getContentRaw();
                Candidate candidate = candidateManager.getCandidate(event.getAuthor().getId());
                String prefix;
                Server server = null;
                boolean isPrivateMessage = channel instanceof PrivateChannel;
                if (isPrivateMessage) {
                    prefix = "?";
                } else {
                    server = serverManager.getServer(event.getGuild().getId());
                    prefix = server.prefix();
                }
                List<String> tokens;
                //check first if user is banned, if not check for server ban or private message
                if (!candidate.isBanned() && (isPrivateMessage || !server.banned())) {
                    //check for prefix
                    if (findPrefix(messageContent, prefix)) {
                        //trim prefix and trailing spaces from command
                        messageContent = messageContent.substring(prefix.length()).trim();

                        //Need to wrap the stringList in an arrayList as stringList does not support removal of indices
                        tokens = new ArrayList<>(Arrays.asList(messageContent.split("\\s+")));

                        TextCommand command = findCommand(tokens);

                        //Check if command was found and that user isn't rate limited
                        if (command != null) {

                            logger.trace("Found command: {}", command.getDefaultName());
                            /*
                             * command was found, check that user is not rate limited and that they have permission
                             */
                            rateLimiter.incrementOrInsertRecord(candidate.getId(), command.getCost());

                            if (rateLimiter.userPermitted(candidate.getId())) {

                                logger.trace("User {} passed rate limit check.", candidate.getId());

                                if (candidate.hasPermission(command.getAccessLevel())) {

                                    logger.trace("User {} has permission to use command {}", candidate.getId(), command.getDefaultName());

                                    if (command.isPrivateMessageCompatible() || !(channel instanceof PrivateChannel)) {
                                        //all checks passed, execute command
                                        try {
                                            Runnable execution = () -> {
                                                if (command.sendTyping()) {
                                                    channel.sendTyping().complete();
                                                }

                                                try {
                                                    command.run(event, tokens);
                                                } catch (KeithExecutionException e) {
                                                    Utilities.Messages.sendError(channel, "Something went wrong :(", e.getMessage());
                                                } catch (KeithPermissionException e) {
                                                    sendMessage(channel, "You do not have access to this command");
                                                }

                                                try {
                                                    candidateManager.incrementCommandCount(candidate.getId());
                                                } catch (Exception e) {
                                                    logger.error("Could not increment command count for user {}", candidate.getId(), e);
                                                }
                                            };
                                            commandService.submit(execution).get(command.getTimeOut(), TimeUnit.SECONDS);
                                        } catch (PermissionException e) {
                                            Utilities.Messages.sendError(channel, "I need more permissions to do that!", e.getMessage());
                                        } catch (IllegalArgumentException e) {
                                            Utilities.Messages.sendError(channel, "Invalid Arguments", e.getMessage());
                                        } catch (TimeoutException e) {
                                            Utilities.Messages.sendError(channel, "Timout", "Execution of command took too long.");
                                        }
                                    } else {
                                        sendMessage(channel, command.getDefaultName() + " cannot be used in private message!");
                                    }

                                } else {
                                    logger.trace("User {} does not have permission to use command {}", candidate.getId(), command.getDefaultName());

                                    sendMessage(channel, "You do not have access to this command");
                                }
                            } else {

                                logger.trace("User {} has been rate limited.", candidate.getId());

                                sendMessage(channel, "Too many commands in a short time.. please wait 30 seconds");
                            }

                        }
                    } else if (channelCommandManager.gameInProgress(channel.getId())) {
                        tokens = new ArrayList<>(Arrays.asList(messageContent.trim().split("\\s+")));
                        ChannelCommandDriver cc = channelCommandManager.getGame(channel.getId());
                        cc.evaluate(message, tokens, candidate);
                    } else if (chatManager.hasActiveChat(channel.getId())) {
                        chatManager.sendMessage(channel.getId(), event);
                    }
                    //else ignore
                }

            } catch (Throwable e) {
                logger.error(e.getMessage(), e);

                Utilities.Messages.sendError(channel, "Something went wrong :(", e.getMessage());
            }
        });
    }

    private boolean findPrefix(String message, String prefix) {
        //ensure that message content greater than prefix length then check if prefix is there
        return message.length() > prefix.length() && message.toLowerCase().startsWith(prefix);
    }

    private TextCommand findCommand(List<String> list) {
        String commandString = list.removeFirst().toLowerCase();
        return commands.get(commandString);
    }

    //wrapper for channel.sendMessage(content).queue() so dont have to write it in full every time.
    private void sendMessage(MessageChannel channel, String content) {
        channel.sendMessage(content).queue();
    }
}

