package succ.commands.admin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.Database;
import succ.util.ServerManager;
import succ.util.UserManager;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * super admin (level 4) command, various commands to interact with bot
 * at core level without checks.
 */
public class BotUtils extends AdminCommand{


    private UserManager userManager;
    private ServerManager serverManager;
    private Database database;
    private long lastReconnect;

    public BotUtils(UserManager userManager, ServerManager serverManager, Database database){
        this.userManager = userManager;
        this.serverManager = serverManager;
        this.database = database;
        lastReconnect = 0;
    }


    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "utils: \"secret stuff\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        String raw = message.getContentRaw().trim().toLowerCase();
        String[] args = raw.split("\\s+");
        String command = args[2];
        try{
            switch(command){
                case "kill":
                    System.exit(0);
                    break;
                case "find":
                    if(args[3].equals("server")){
                        Guild server =event.getJDA().getGuildById(args[4]);
                        channel.sendMessage(server.toString()).queue();
                    } else if(args[3].equals("user")){
                        User user = event.getJDA().getUserById(args[4]);
                        channel.sendMessage(user.getName()+"#"+user.getDiscriminator()).queue();
                    }
                    break;
                case "uptime":
                    channel.sendMessage(getUptime()).queue();
                    break;
                case "db":
                    channel.sendMessage(query(raw.substring(raw.indexOf(args[3])))).queue();
                    break;
                case "database":
                    channel.sendMessage(query(raw.substring(raw.indexOf(args[3])))).queue();
                    break;
            }
        } catch(IndexOutOfBoundsException e){
               channel.sendMessage("Insufficient arguments, try again").queue();
        }
    }

    @Override
    public int getAccessLevel(){
        return 4;
    }

    private String getUptime(){
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime()-lastReconnect;
        long days = TimeUnit.MILLISECONDS.toDays(uptime);
        long hours = TimeUnit.MILLISECONDS.toHours(uptime) % TimeUnit.DAYS.toHours(1);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptime) % TimeUnit.MINUTES.toSeconds(1);
        //Format response, only include larger measurement if relevant.
        String response = "";
        if(days>0)
            response+=(days==1) ? days+" day, " : days+" days, ";
        if(hours>0)
            response+=(hours==1) ? hours+" hour, " : hours+" hours, ";
        if(minutes>0)
            response+=(minutes==1) ? minutes+" minute, " : minutes+" minutes, ";
        if(seconds>0)
            response+=(seconds==1) ? seconds+" second" : seconds+" seconds";

        return response;
    }

    public void updateUptime(){
        lastReconnect=ManagementFactory.getRuntimeMXBean().getUptime();
    }

    private String query(String searchTerm){
        ArrayList<String> results = database.query(searchTerm);
        int n = 0;
        String result="```";
        for(String resultSet : results){
            if(n<=20){
            result+=resultSet+"\n";
            }
            n++;
        }
        result+="```";
        return result;
    }
}
