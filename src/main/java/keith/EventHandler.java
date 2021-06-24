package keith;

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
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandler extends ListenerAdapter {

    ExecutorService commandService;
    ServerManager serverManager;
    UserManager userManager;

    public EventHandler(DataSource database, JDA jda) {
        initialise(database, jda);
    }

    private void initialise(DataSource database, JDA jda) {
        initialiseCommands();
        jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));
        commandService = Executors.newCachedThreadPool();
        serverManager = ServerManager.getInstance();
        userManager = UserManager.getInstance();
        Database.setSource(database);
        Utilities.setJDA(jda);
    }

    private void initialiseCommands() {

    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(() -> {
            Server server = serverManager.getServer(event.getGuild().getId());
            User user = userManager.getUser(event.getAuthor().getId());
            Message message = event.getMessage();
            //if user or server not banned
            if(!user.isBanned() && !server.isBanned() && !event.getAuthor().isBot()) {
                //check for prefix
                if(findPrefix(message, server)) {
                    //check for valid command
                        //execute
                    //else check for channel command
                        //process
                    //else ignore
                }
            }

        }).start();
    }

    private boolean findPrefix(Message message, Server server) {
        //ensure that message content greater than prefix length then check if prefix is there
        return message.getContentRaw().length() > server.getPrefix().length() && message.getContentRaw().startsWith(server.getPrefix());
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