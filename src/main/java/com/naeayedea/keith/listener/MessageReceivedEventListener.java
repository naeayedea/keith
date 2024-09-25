package com.naeayedea.keith.listener;

import com.naeayedea.keith.commands.IMessageCommand;
import com.naeayedea.keith.commands.admin.Admin;
import com.naeayedea.keith.commands.channel_commands.IChannelCommand;
import com.naeayedea.keith.commands.generic.*;
import com.naeayedea.keith.commands.info.Help;
import com.naeayedea.keith.commands.info.Invite;
import com.naeayedea.keith.managers.ChannelCommandManager;
import com.naeayedea.keith.managers.ServerChatManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.managers.UserManager;
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

    private MultiMap<String, IMessageCommand> commands;

    private final ExecutorService messageService;
    private final ExecutorService commandService;

    private final UserManager userManager;
    private final ServerManager serverManager;
    private final ChannelCommandManager channelCommandManager;
    private final ServerChatManager chatManager;
    private final CommandRateLimiter rateLimiter;

    public MessageReceivedEventListener(@Qualifier("messageService") ExecutorService messageService, @Qualifier("commandService") ExecutorService commandService, UserManager userManager, ServerManager serverManager, ChannelCommandManager channelCommandManager, ServerChatManager chatManager, CommandRateLimiter rateLimiter) {
        this.messageService = messageService;
        this.userManager = userManager;
        this.serverManager = serverManager;
        this.channelCommandManager = channelCommandManager;
        this.chatManager = chatManager;
        this.commandService = commandService;
        this.rateLimiter = rateLimiter;
    }

    @PostConstruct
    private void initialiseCommands() {
        logger.info("Initializing command map.");

        //message commands
        commands = new MultiMap<>();
        commands.putAll(Arrays.asList("help", "hlep", "commands"), new Help(commands));
        commands.putAll(Arrays.asList("guess", "numguess"), new Guess());
        commands.putAll(Arrays.asList("avatar", "picture", "pfp"), new Avatar());
        commands.putAll(Arrays.asList("pin", "sticky"), new Pin());
        commands.putAll(Arrays.asList("remind", "remindme"), new Remind());
        commands.putAll(Arrays.asList("calculator", "calc", "calculate", "evaluate"), new Calculator());
        commands.putAll(Arrays.asList("servericon", "guildicon", "icon", "serveravatar", "guildavatar"), new ServerIcon());
        commands.putAll(Arrays.asList("otd", "onthisday", "events", "history"), new OnThisDay());
        commands.putAll(Arrays.asList("banner", "getbanner", "header"), new Banner());
        commands.putAll(Arrays.asList("chat", "serverchat"), new Chat());
        commands.put("setprefix", new SetPrefix());
        commands.putAll(Arrays.asList("admin", "sudo"), new Admin());
        commands.put("invite", new Invite());
        commands.put("feedback", new Feedback());
        commands.put("interpret", new Interpret());

        logger.info("Loaded {} message commmands", commands.size());
    }

    @EventListener(MessageReceivedEvent.class)
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        messageService.submit(() -> {
            if (!event.getAuthor().isBot()) {
                MessageChannel channel = event.getChannel();
                Message message = event.getMessage();
                //automatically join any threads that are created so that bot feels easy to use in threads
                if (channel instanceof ThreadChannel thread && !thread.isJoined()) {
                    thread.join().queue();
                }

                String messageContent = message.getContentRaw();
                UserManager.User user = userManager.getUser(event.getAuthor().getId());
                String prefix;
                ServerManager.Server server = null;
                boolean isPrivateMessage = channel instanceof PrivateChannel;
                if (isPrivateMessage) {
                    prefix = "?";
                } else {
                    server = serverManager.getServer(event.getGuild().getId());
                    prefix = server.getPrefix();
                }
                List<String> tokens;
                //check first if user is banned, if not check for server ban or private message
                if (!user.isBanned() && (isPrivateMessage || !server.isBanned())) {
                    //check for prefix
                    if (findPrefix(messageContent, prefix)) {
                        //trim prefix and trailing spaces from command
                        messageContent = messageContent.substring(prefix.length()).trim();

                        //Need to wrap the stringList in an arrayList as stringList does not support removal of indices
                        tokens = new ArrayList<>(Arrays.asList(messageContent.split("\\s+")));

                        IMessageCommand command = findCommand(tokens);

                        //Check if command was found and that user isn't rate limited
                        if (command != null ) {
                            /*
                             * command was found, check that user is not rate limited and that they have permission
                             */
                            rateLimiter.incrementOrInsertRecord(user.getId(), command.getCost());

                            if (rateLimiter.userPermitted(user.getId())) {
                                if (user.hasPermission(command.getAccessLevel())) {
                                    if (command.isPrivateMessageCompatible() || !(channel instanceof PrivateChannel)) {
                                        //all checks passed, execute command
                                        try {
                                            Runnable execution = () -> {
                                                if (command.sendTyping()) {
                                                    channel.sendTyping().queue();
                                                }
                                                command.run(event, tokens);
                                                user.incrementCommandCount();
                                            };
                                            commandService.submit(execution).get(command.getTimeOut(), TimeUnit.SECONDS);
                                        } catch (PermissionException e) {
                                            Utilities.Messages.sendError(channel,"Insufficient Permissions to do that!", e.getMessage());
                                        } catch (IllegalArgumentException e) {
                                            Utilities.Messages.sendError(channel, "Invalid Arguments", e.getMessage());
                                        } catch (TimeoutException e) {
                                            Utilities.Messages.sendError(channel, "Timout","Execution of command took too long.");
                                        } catch (Throwable e) {
                                            Utilities.Messages.sendError(channel, "Something went wrong :(", e.getMessage());
                                        }
                                    } else {
                                        sendMessage(channel, command.getDefaultName()+" cannot be used in private message!");
                                    }
                                } else {
                                    sendMessage(channel, "You do not have access to this command");
                                }
                            } else {
                                sendMessage(channel, "Too many commands in a short time.. please wait 30 seconds");
                            }

                            /*
                             * No command was found so check for any currently running channel commands
                             */
                        }
                        //else ignore
                    } else if (channelCommandManager.gameInProgress(channel.getId())) {
                        tokens = new ArrayList<>(Arrays.asList(messageContent.trim().split("\\s+")));
                        IChannelCommand cc = channelCommandManager.getGame(channel.getId());
                        cc.evaluate(message, tokens, user);
                    } else if (chatManager.hasActiveChat(channel.getId())) {
                        chatManager.sendMessage(channel.getId(), event);
                    }
                }
            }
        });
    }

    private boolean findPrefix(String message, String prefix) {
        //ensure that message content greater than prefix length then check if prefix is there
        return message.length() > prefix.length() && message.toLowerCase().startsWith(prefix);
    }

    private IMessageCommand findCommand(List<String> list) {
        String commandString = list.remove(0).toLowerCase();
        return commands.get(commandString);
    }

    //wrapper for channel.sendMessage(content).queue() so dont have to write it in full every time.
    private void sendMessage(MessageChannel channel, String content) {
        channel.sendMessage(content).queue();
    }
}

