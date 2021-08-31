package keith;

import keith.commands.Command;
import keith.managers.ServerManager;
import keith.managers.ServerManager.Server;
import keith.managers.UserManager;
import keith.managers.UserManager.User;
import keith.util.Database;
import keith.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.*;

public class EventHandler extends ListenerAdapter {

    ExecutorService commandService;
    ScheduledExecutorService rateLimitService;
    Map<String, Integer> rateLimitRecord;
    ServerManager serverManager;
    UserManager userManager;

    public EventHandler(DataSource database, JDA jda) {
        initialise(database, jda);
    }

    private void initialise(DataSource database, JDA jda) {
        initialiseCommands();
        jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));
        commandService = Executors.newCachedThreadPool();
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

    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(() -> {
            Server server = serverManager.getServer(event.getGuild().getId());
            User user = userManager.getUser(event.getAuthor().getId());
            Message message = event.getMessage();
            String messageContent = message.getContentRaw().toLowerCase();
            //if user or server not banned
            if (!user.isBanned() && !server.isBanned() && !event.getAuthor().isBot()) {
                //check for prefix
                if (findPrefix(messageContent, server)) {
                    //trim prefix and trailing spaces from command
                    Command command = findCommand(messageContent.substring(server.getPrefix().length()).trim());
                    Integer numRecentCommands = rateLimitRecord.get(user.getDiscordID());
                    if (command != null && (numRecentCommands != null && numRecentCommands < Utilities.getRateLimitMax())) {
                        //execute command


                    }
                    //else check for channel command

                    //else ignore
                }
            }

        }).start();
    }

    private boolean findPrefix(String message, Server server) {
        //ensure that message content greater than prefix length then check if prefix is there
        return message.length() > server.getPrefix().length() && message.startsWith(server.getPrefix());
    }

    private Command findCommand(String message) {
        return null;
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