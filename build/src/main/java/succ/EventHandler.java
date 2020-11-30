package succ;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import succ.commands.admin.Admin;
import succ.commands.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import succ.commands.admin.SetStatus;
import succ.commands.generic.*;
import succ.util.Database;
import succ.util.Server;
import succ.util.ServerManager;
import succ.util.UserManager;
import succ.util.logs.ConsoleLogger;
import succ.util.Emoji;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Main message handler for keithv2, receives incoming messages and performs tasks depending on message content
 */
public class EventHandler extends ListenerAdapter {

    JDA jda;
    Map<String, Command> commands;

    Database database;
    ConsoleLogger log;
    UserManager userManager;
    ServerManager serverManager;
    ExecutorService commandExecutor;
    public EventHandler(JDA jda, String url){
        this.jda = jda;
        database = new Database(url);
        log = new ConsoleLogger();
        userManager = new UserManager(database);
        serverManager = new ServerManager(database);
        initialise();
    }

    private void initialise(){
        initialiseCommands();
        commandExecutor = Executors.newCachedThreadPool();
        jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));  //Default discord status
        ((Remind) commands.get("remind")).loadReminders();
    }

    //Populate hashmap with all available commands, when key is entered the relevant command is returned which can be ran.
    private void initialiseCommands(){
        commands = new HashMap<String, Command>();
        commands.put("admin", new Admin(database, jda, serverManager));
        commands.put("avatar", new Avatar());
        commands.put("calc", new Calculator());
        commands.put("setprefix", new SetPrefix(serverManager));
        commands.put("feedback", new Feedback(userManager));
        commands.put("guess", new Guess(serverManager, 40));
        commands.put("remind", new Remind(database, jda));
        commands.put("emoji", new EmojiRole(database,serverManager, jda));
        commands.put("help", new Help(commands, serverManager)); //Always initialise help last
    }


    //Search the start of the users message for the prefix, whatever that may be.
    private boolean detectPrefix(MessageReceivedEvent event, String prefix){
        try{
            String message = event.getMessage().getContentRaw();
            String prefix_search = message.substring(0,prefix.length());

            return prefix_search.equals(prefix) && message.length()>prefix.length();
        } catch (StringIndexOutOfBoundsException c) {
            return false;
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        new Thread( () -> {
            if(event.getAuthor().isBot() ||  userManager.getUser(event.getAuthor().getId()).getAccessLevel()==0)      //Filter out bots, banned users and servers, waste no time on it.
                return;
            if(!(event.getChannel() instanceof PrivateChannel) && serverManager.getServer(event.getGuild().getId()).isBanned())
                return;

            String prefix;
            //Retrieve servers prefix, if the message is from a private channel -> default prefix
            if(!(event.getChannel() instanceof PrivateChannel))
                prefix = serverManager.getServer(event.getGuild().getId()).getPrefix();
            else
                prefix = "?";

            //If someone tags the bot, tell them the prefix
            if(event.getMessage().getMentionedUsers().contains(jda.getSelfUser()) && (event.getMessage().getContentRaw().contains("prefix") || event.getMessage().getContentRaw().contains("help"))){
                event.getChannel().sendMessage("The current prefix is: "+prefix).queue();
            }

            User user = event.getAuthor();
            if(!(userManager.getUser(user.getId()).getAccessLevel()==0)){
                if(event.getChannel() instanceof TextChannel){
                    publicMessageReceived(event);                   //Log operations
                }
                if(event.getChannel() instanceof PrivateChannel){
                    privateMessageReceived(event);                  //Log operations
                }
                if(detectPrefix(event, prefix)){                            //Search beginning of message for server prefix
                    Command command = findCommand(event, prefix);
                    if(command!=null && userManager.getUser(user.getId()).getAccessLevel()>=command.getAccessLevel()){                              //If command found, perform.
                        if(!(userManager.getUser(user.getId()).getCommandCount() > 0)){      //check if user has used bot before, if no send welcome message
                            event.getChannel().sendMessage("Hi "+user.getAsMention()+", thank you for using keith! Type \""+prefix+"help\" to see a list of commands!").queue();
                        }
                        try {
                            Runnable execution = () -> {command.run(event); userManager.incrementCommandCount(user.getId());};
                            commandExecutor.submit(execution).get(command.getTimeOut(), TimeUnit.SECONDS); //run command operations, kill after max time reached
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            event.getChannel().sendMessage("Something went wrong :(").queue();
                        } catch (TimeoutException e){
                                event.getChannel().sendMessage("Command took to long to execute!").queue();
                        }
                    }
                    else if(command!=null){
                        event.getChannel().sendMessage("You do not have permission to use this command!").queue();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event){
        //Attempt to retrieve server from database, will create new entry if not there
        Guild guild = event.getGuild();
        Server server = serverManager.getServer(event.getGuild().getId());
        TextChannel defaultChannel = guild.getDefaultChannel();
        defaultChannel.sendMessage(new EmbedBuilder()
                .setColor(new Color(155,0,155))
                .setTitle("Hello!")
                .setFooter("Use "+server.getPrefix()+"feedback if you have any issues!- Succ")
                .setDescription("Use "+server.getPrefix()+"help to see available commands")
                .setThumbnail(jda.getSelfUser().getAvatarUrl())
                .build()).queue();
        log.printSuccess("New Server "+guild+" has added the bot!");
        new SetStatus(jda, serverManager).update();
    }


    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event){
        new Thread(() -> {
            EmojiRole driver = (EmojiRole) commands.get("emoji");
            Map<String, Boolean> activeEmojis = driver.getActiveEmojis();
            ArrayList<Emoji> emojiRoles = driver.getEmojis();
            //Filter out banned users and private channels
            if(event.getUser().isBot() || event.getChannel() instanceof PrivateChannel ||  userManager.getUser(event.getUser().getId()).getAccessLevel()==0)
                return;
            if(serverManager.getServer(event.getGuild().getId()).isBanned())
                return;
            //Determine if guild has a role message channel and if the current channel is that channel
            Message roleMessage = serverManager.getRoleMessage(event.getGuild());
            if(roleMessage==null || !(event.getReaction().getMessageId().equals(roleMessage.getId())))
                return;
            MessageReaction.ReactionEmote emote = event.getReactionEmote();
            String emojiString;
            // Check if emote is a unicode emote
            if(emote.isEmoji()){
                Boolean active = activeEmojis.get(emote.getEmoji());
                if(active==null || !active)
                    return;
                emojiString = emote.getEmoji();
                event.getChannel().retrieveMessageById(event.getMessageId()).queue((message) -> {
                    //Identified unicode emoji, do stuff
                    for(Emoji emoji : emojiRoles){
                        if(emoji.getEmoji().equals(emojiString) && emoji.getServerId()==event.getGuild().getIdLong()){
                            Guild guild = event.getGuild();
                            Role role = guild.getRoleById(emoji.getRoleId());
                            System.out.println(role);
                            guild.addRoleToMember(guild.getMember(event.getUser()), role).queue();
                            return;
                        }
                    }
                });
            }
            else {
                Boolean active = activeEmojis.get(emote.getId());
                if(active==null || !active){
                    System.out.println("not active");
                    return;
                }
                emojiString = emote.getId();
                event.getChannel().retrieveMessageById(event.getMessageId()).queue((message) -> {
                    for(Emoji emoji : emojiRoles){
                        if(emoji.getEmoji().equals(emojiString)){
                            Guild server = jda.getGuildById(emoji.getSourceId());
                            Emote roleEmote = server.getEmoteById(emoji.getEmoji());
                            if(roleEmote!=null){
                                //Identified custom emoji, do stuff
                                Guild guild = event.getGuild();
                                Role role = guild.getRoleById(emoji.getRoleId());
                                try{
                                guild.addRoleToMember(guild.getMember(event.getUser()), role).queue();
                                    return;
                                }
                                catch(PermissionException e){
                                    //Dont have permissions, cant do
                                    return;
                                }
                            }
                            System.out.println(":(");
                        }
                    }

                });
            }
        }).start();
    }

    @Override
    public void onReconnect(ReconnectedEvent event){
        Command admin = commands.get("admin");
        ((Admin) admin).updateUptime();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event){
        log.printWarning("Server "+event.getGuild()+" has kicked the bot :(");
        new SetStatus(jda, serverManager).update();
    }
                             //Performs tasks relevant to public messages
    private void publicMessageReceived(MessageReceivedEvent event){
        log.printPublicMessage(event.getAuthor().getName()+": "+event.getMessage().getContentDisplay());
    }

    //Performs tasks relevant to private messages, stores in private message database.
    private void privateMessageReceived(MessageReceivedEvent event){
        log.printPrivateMessage(event.getAuthor().getName()+": "+event.getMessage().getContentDisplay());
    }

    private Command findCommand(MessageReceivedEvent event, String prefix){
        String command = event.getMessage().getContentDisplay();
        command = command.substring(prefix.length());
        String[] commandSplit = command.split("\\s+");
        return commands.get(commandSplit[0]);
    }
}
