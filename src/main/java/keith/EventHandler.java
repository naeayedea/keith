package keith;

import keith.managers.ServerManager;
import keith.managers.UserManager;
import keith.util.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.sql.DataSource;
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
    }

    private void initialiseCommands() {

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        new Thread(() -> {
            ServerManager.Server server = serverManager.getServer(event.getGuild().getId());
            UserManager.User user = userManager.getUser(event.getAuthor().getId());
            Message message = event.getMessage();
            //if user or server not banned
            if(!user.isBanned() && !server.isBanned() && !event.getAuthor().isBot()) {
                //check for prefix
                if(message.getContentRaw().substring(server.getPrefix().length()).equals(server.getPrefix())) {
                    //check for valid command
                        //execute
                    //else check for channel command
                        //process
                    //else ignore
                }
            }

        }).start();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event){
        Guild guild = event.getGuild();

    }

    @Override
    public void onReconnected(ReconnectedEvent event){

    }


}