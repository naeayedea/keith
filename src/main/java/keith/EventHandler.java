package keith;

import keith.commands.Command;
import keith.commands.channel_commands.ChannelCommand;
import keith.commands.generic.Guess;
import keith.commands.info.Help;
import keith.managers.ChannelCommandManager;
import keith.managers.ServerManager;
import keith.managers.ServerManager.Server;
import keith.managers.UserManager;
import keith.managers.UserManager.User;
import keith.util.Database;
import keith.util.MultiMap;
import keith.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class EventHandler extends ListenerAdapter {

    ExecutorService commandService;
    ChannelCommandManager channelCommandService;
    ScheduledExecutorService rateLimitService;
    Map<String, Integer> rateLimitRecord;
    MultiMap<String, Command> commands;
    ServerManager serverManager;
    UserManager userManager;

    public EventHandler(DataSource database, JDA jda) {
        initialise(database, jda);
    }

    private void initialise(DataSource database, JDA jda) {
        initialiseCommands();
        jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));
        commandService = Executors.newCachedThreadPool();
        channelCommandService = ChannelCommandManager.getInstance();
        rateLimitService = Executors.newScheduledThreadPool(1);
        rateLimitRecord = new ConcurrentHashMap<>();
        Runnable clearHashMap = () -> rateLimitRecord.clear();
        rateLimitService.scheduleAtFixedRate(clearHashMap, 30, 30, TimeUnit.SECONDS);
        serverManager = ServerManager.getInstance();
        userManager = UserManager.getInstance();
        Database.setSource(database);
        Utilities.setJDA(jda);
        Utilities.setRateLimitMax(5);
    }

    private void initialiseCommands() {
        commands = new MultiMap<>();
        commands.putAll(Arrays.asList("help", "test", "what", "5"), new Help(commands, "help"));
        commands.put("settings", commands.get("help"));
        commands.put("guess", new Guess());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(() -> {
            if (!event.getAuthor().isBot()) {
                MessageChannel channel = event.getChannel();
                Message message = event.getMessage();
                String messageContent = message.getContentRaw().toLowerCase();
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
                        Integer numRecentCommands = rateLimitRecord.get(user.getDiscordID());

                        //Check if command was found and that user isn't rate limited
                        if (command != null ) {
                            /*
                             * command was found, check that user is not rate limited and that they have permission
                             */
                            if (numRecentCommands == null || numRecentCommands < Utilities.getRateLimitMax()) {
                                if (user.hasPermission(command.getAccessLevel())) {
                                    //all checks passed, execute command
                                    try {
                                        Runnable execution = () -> {
                                            channel.sendTyping().queue(); //THIS IS TEMPORARY UNTIL ITS DECIDED WHICH COMMANDS SHOULD SAY TYPING..
                                            command.run(event, tokens);
                                            user.incrementCommandCount();
                                            //TODO: Implement rate limiting by adding "cost" of each command to rate limit map.
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
                                        sendMessage(channel, "You do not have access to this command");
                                }
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
                    }
                }
            }
        }).start();
    }

    private boolean findPrefix(String message, String prefix) {
        //ensure that message content greater than prefix length then check if prefix is there
        return message.length() > prefix.length() && message.startsWith(prefix);
    }

    private Command findCommand(List<String> list) {
        String commandString = list.remove(0);
        return commands.get(commandString);
    }

    //wrapper for channel.sendMesssage(content).queue() so dont have to write it in full every time.
    private void sendMessage(MessageChannel channel, String content) {
        channel.sendMessage(content).queue();
    }

    private void sendEmbed(MessageChannel channel, MessageEmbed embed) {
        channel.sendMessage(embed).queue();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event){
        Guild guild = event.getGuild();

    }

    @Override
    public void onReconnected(@NotNull ReconnectedEvent event){
        Utilities.updateUptime();
        Utilities.setJDA(event.getJDA());

    }


}