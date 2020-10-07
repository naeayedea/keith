package succ.commands.admin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.Database;
import succ.util.ServerManager;

import java.util.ArrayList;
import java.util.Arrays;

public class Stats extends AdminCommand{

    private Database database;
    private ServerManager serverManager;
    private JDA jda;

    public Stats(Database database, ServerManager serverManager){
        this.database=database;
        this.serverManager = serverManager;
    }
                 @Override
    public String getDescription(MessageReceivedEvent event) {
        return "stats: \"returns various bot stats do '"+super.getPrefix(event, serverManager)+"admin statis (servers/users/admins)'\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        jda = event.getJDA();
        jda.getGuilds();
        MessageChannel channel = event.getChannel();
        String commandRaw = event.getMessage().getContentDisplay();
        String[] commandSplit = commandRaw.split("\\s+");
        String type = commandSplit[2];
        ArrayList<String> results;
        switch(type){
            case "admins":
                results = returnAdmins();
                String adminList = "```";
                    for(String discordid : results){
                        System.out.println(discordid.trim());
                        User user = jda.getUserById(discordid.trim());
                        adminList+=user.getName()+"("+user.getId()+")\n";
                    }
                    channel.sendMessage("Admin Users:\n"+adminList+"```").queue();
                break;
            case "servers":
                results = returnServers();
                String serverList = "```";
                for(String resultSet : results){
                    String[] args = resultSet.split("\\s+");
                    System.out.println(Arrays.toString(args));
                    String serverId = args[0].trim();
                    String firstSeen = args[1];
                    Guild server = jda.getGuildById(serverId);
                    if(server!=null)
                    serverList+= server.getName()+"("+server.getId()+") First Seen: "+firstSeen+"\n";
                }
                channel.sendMessage("Servers:\n"+serverList+"```").queue();
                break;
            case "users":
                channel.sendMessage("User count: "+returnUserCount()).queue();
                break;
            default:
                channel.sendMessage("fuck").queue();
        }
    }

    private ArrayList<String> returnAdmins(){
        return database.query("SELECT (DiscordID) FROM users WHERE UserLevel > 2");
    }

    private ArrayList<String> returnServers(){
        return database.query("SELECT ServerID, FirstSeen FROM servers");
    }

    private int returnUserCount(){
        return jda.getUsers().size();
    }

}
