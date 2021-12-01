package keith;

import keith.commands.Command;
import keith.commands.admin.Admin;
import keith.commands.channel_commands.ChannelCommand;
import keith.commands.generic.*;
import keith.commands.info.Help;
import keith.commands.info.Invite;
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
        jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));
        commandService = Executors.newCachedThreadPool();
        channelCommandService = ChannelCommandManager.getInstance();
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
        Utilities.setJDA(jda);
        Utilities.setRateLimitMax(7);
        initialiseCommands();
    }

    private void initialiseCommands() {
        commands = new MultiMap<>();
        commands.putAll(Arrays.asList("help", "hlep", "commands"), new Help(commands));
        commands.putAll(Arrays.asList("guess", "numguess"), new Guess());
        commands.putAll(Arrays.asList("avatar", "picture", "pfp"), new Avatar());
        commands.putAll(Arrays.asList("pin", "sticky"), new Pin());
        commands.putAll(Arrays.asList("remind", "remindme"), new Remind());
        commands.putAll(Arrays.asList("calculator", "calc", "calculate", "evaluate"), new Calculator());
        commands.putAll(Arrays.asList("servericon", "guildicon", "icon", "serveravatar", "guildavatar"), new ServerIcon());
        commands.put("setprefix", new SetPrefix());
        commands.putAll(Arrays.asList("admin", "sudo"), new Admin());
        commands.put("invite", new Invite());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(() -> {
            if (!event.getAuthor().isBot()) {
                MessageChannel channel = event.getChannel();
                Message message = event.getMessage();
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

    //wrapper for channel.sendMesssage(content).queue() so dont have to write it in full every time.
    private void sendMessage(MessageChannel channel, String content) {
        channel.sendMessage(content).queue();
    }

    private void sendEmbed(MessageChannel channel, MessageEmbed embed) {
        channel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event){
        Guild guild = event.getGuild();

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