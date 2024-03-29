package keith;

import keith.commands.Command;
import keith.commands.IReactionCommand;
import keith.commands.admin.Admin;
import keith.commands.channel_commands.ChannelCommand;
import keith.commands.generic.*;
import keith.commands.info.Help;
import keith.commands.info.Invite;
import keith.managers.ChannelCommandManager;
import keith.managers.ServerChatManager;
import keith.managers.ServerManager;
import keith.managers.ServerManager.Server;
import keith.managers.UserManager;
import keith.managers.UserManager.User;
import keith.util.Database;
import keith.util.MultiMap;
import keith.util.Utilities;
import keith.util.logs.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class EventHandler extends ListenerAdapter {

    ExecutorService commandService;
    ChannelCommandManager channelCommandService;
    ServerChatManager chatManager;
    ScheduledExecutorService rateLimitService;
    Map<String, Integer> rateLimitRecord;
    MultiMap<String, Command> commands;
    MultiMap<String, IReactionCommand> reactionCommands;
    ServerManager serverManager;
    UserManager userManager;

    public EventHandler(DataSource database, JDA jda, String restartMessage, String restartChannel) {
        if(!restartMessage.equals("")) {
            TextChannel channel = jda.getTextChannelById(restartChannel);
            if (channel != null) {
                channel.retrieveMessageById(restartMessage).queue(message -> message.editMessage("Restarted").queue());
            }
        }
        initialise(database, jda);
    }

    private void initialise(DataSource database, JDA jda) {
        Utilities.setJDA(jda);
        Utilities.setRateLimitMax(7);
        jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));
        commandService = Executors.newCachedThreadPool();
        channelCommandService = ChannelCommandManager.getInstance();
        chatManager = ServerChatManager.getInstance();
        rateLimitService = Executors.newScheduledThreadPool(1);
        rateLimitRecord = new ConcurrentHashMap<>();
        serverManager = ServerManager.getInstance();
        userManager = UserManager.getInstance();
        Runnable clearHistory = () -> {
            rateLimitRecord.clear();
            serverManager.clear();
            userManager.clear();
        };
        rateLimitService.scheduleAtFixedRate(clearHistory, 30, 30, TimeUnit.SECONDS);
        Database.setSource(database);
        initialiseCommands();
    }

    private void initialiseCommands() {
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

        //reaction commands
        reactionCommands = new MultiMap<>();
        reactionCommands.put("📌", new Pin());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(() -> {
            if (!event.getAuthor().isBot()) {
                MessageChannel channel = event.getChannel();
                Message message = event.getMessage();
                //automatically join any threads that are created so that bot feels easy to use in threads
                if (channel instanceof ThreadChannel) {
                    ThreadChannel thread = ((ThreadChannel) channel);
                    if (!thread.isJoined()) {
                        thread.join().queue();
                    }
                }
                String messageContent = message.getContentRaw();
                User user = userManager.getUser(event.getAuthor().getId());
                String prefix;
                Server server = null;
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
                        Command command = findCommand(tokens);
                        Integer numRecentCommandsObj = rateLimitRecord.get(user.getId());
                        int numRecentCommands = numRecentCommandsObj == null ? 0 : numRecentCommandsObj;

                        //Check if command was found and that user isn't rate limited
                        if (command != null ) {
                            /*
                             * command was found, check that user is not rate limited and that they have permission
                             */
                            rateLimitRecord.put(user.getId(), numRecentCommands + 1);
                            if (numRecentCommands < Utilities.getRateLimitMax()) {
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
                                        } catch (Exception e) {
                                            Utilities.Messages.sendError(channel, "Something went wrong :(", e.getMessage());
                                        }
                                    } else {
                                        sendMessage(channel, command.getDefaultName()+" cannot be used in private message!");
                                    }
                                } else {
                                        sendMessage(channel, "You do not have access to this command");
                                }
                            } else if (numRecentCommands == Utilities.getRateLimitMax()) {
                                sendMessage(channel, "Too many commands in a short time.. please wait 30 seconds");
                            }

                            /*
                             * No command was found so check for any currently running channel commands
                             */
                        }
                        //else ignore
                    } else if (channelCommandService.gameInProgress(channel.getId())) {
                        tokens = new ArrayList<>(Arrays.asList(messageContent.trim().split("\\s+")));
                        ChannelCommand cc = channelCommandService.getGame(channel.getId());
                        cc.evaluate(message, tokens, user);
                    } else if (chatManager.hasActiveChat(channel.getId())) {
                        chatManager.sendMessage(channel.getId(), event);
                    }
                }
            }
        }).start();
    }

    private boolean findPrefix(String message, String prefix) {
        //ensure that message content greater than prefix length then check if prefix is there
        return message.length() > prefix.length() && message.toLowerCase().startsWith(prefix);
    }

    private Command findCommand(List<String> list) {
        String commandString = list.remove(0).toLowerCase();
        return commands.get(commandString);
    }

    //wrapper for channel.sendMessage(content).queue() so dont have to write it in full every time.
    private void sendMessage(MessageChannel channel, String content) {
        channel.sendMessage(content).queue();
    }

    private void sendEmbed(MessageChannel channel, MessageEmbed embed) {
        channel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        MessageReaction.ReactionEmote emote = event.getReaction().getReactionEmote();
        Member member = event.getMember();
        //ensure that event is not caused by a bot and the emote is a standard emoji
        if (member != null && !member.getUser().isBot() && emote.isEmoji()) {
            MessageChannel channel = event.getChannel();
            //retrieve the message which is being reacted to
            Message message = channel.retrieveMessageById(event.getMessageId()).complete();
            IReactionCommand command = reactionCommands.get(emote.getEmoji());
            //join any threads automatically
            if (channel instanceof ThreadChannel) {
                ThreadChannel thread = ((ThreadChannel) channel);
                if (!thread.isJoined()) {
                    thread.join().queue();
                }
            }
            User user = userManager.getUser(member.getUser().getId());
            boolean isPrivateMessage = channel instanceof PrivateChannel;
            Server server = isPrivateMessage ? null : serverManager.getServer(event.getGuild().getId());
            //ensure that user and server has permission to use the bot
            if (!user.isBanned() && (isPrivateMessage || !server.isBanned())) {
                Integer numRecentCommandsObj = rateLimitRecord.get(user.getId());
                int numRecentCommands = numRecentCommandsObj == null ? 0 : numRecentCommandsObj;
                //make sure that rate limit has not been reached
                if (numRecentCommands < Utilities.getRateLimitMax()) {
                    List<MessageReaction> reactions = message.getReactions();
                    for (MessageReaction reaction : reactions) {
                        if (reaction.getReactionEmote().getEmoji().equals(emote.getEmoji())) {
                            if (reaction.hasCount() && reaction.getCount() < 2) {
                                rateLimitRecord.put(user.getId(), numRecentCommands + 1);
                                command.run(event, member.getUser());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        Server server = serverManager.getServer(event.getGuild().getId());
        TextChannel defaultChannel = guild.getDefaultChannel();
        if (defaultChannel != null) {
            defaultChannel.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(new Color(155,0,155))
                    .setTitle("Hello!")
                    .setFooter("Use "+server.getPrefix()+"feedback if you have any issues!- Succ")
                    .setDescription("Use "+server.getPrefix()+"help to see available commands")
                    .setThumbnail(Utilities.getJDAInstance().getSelfUser().getAvatarUrl())
                    .build()).queue();
            Logger.printSuccess("New Server "+guild+" has added the bot!");
        }
        Utilities.updateDefaultStatus();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Logger.printWarning("Server "+event.getGuild()+" has kicked the bot :(");
        Utilities.updateDefaultStatus();
    }

    @Override
    public void onReconnected(@NotNull ReconnectedEvent event){
        Utilities.updateUptime();
        Utilities.setJDA(event.getJDA());
        UserManager.getInstance().clear();
        ServerManager.getInstance().clear();
        Utilities.updateDefaultStatus();
    }


}
