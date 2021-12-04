package keith.commands.admin;

import keith.managers.UserManager;
import keith.util.Database;
import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class Stats extends AdminCommand {

    String defaultName;
    private static final String VERSION = "3.0.2 \"THREADS AND REMINDERS\"";

    public Stats() {
        defaultName = "stats";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"returns various bot stats from database\"";
    }

    @Override
    public String getLongDescription() {
        return "usage: admin stats (servers/users/admins)";
    }

    @Override
    public String getDefaultName() {
        return "stats";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        JDA jda = Utilities.getJDAInstance();
        MessageChannel channel = event.getChannel();
        String type;
        if(tokens.isEmpty()) {
            sendStats(channel);
        } else  {
            type = tokens.get(0);
            ArrayList<String> results;
            switch(type.toLowerCase()){
                case "admins":
                    results = Database.getStringResult(returnAdmins());
                    StringBuilder adminList = new StringBuilder();
                    for(int i = 1 ; i < results.size(); i++){
                        String discordId = results.get(i).trim();
                        UserManager.User user = UserManager.getInstance().getUser(discordId);
                        adminList.append("> ").append(user.getDescription()).append("\n");
                    }
                    channel.sendMessage("Admin Users:\n"+adminList).queue();
                    break;
                case "servers":
                    results = Database.getStringResult(returnServers());
                    StringBuilder serverList = new StringBuilder();
                    for(int i = 1; i < results.size(); i++){
                        String resultSet = results.get(i);
                        String[] args = resultSet.split("\\s+");
                        String serverId = args[0].trim();
                        String firstSeen = args[1];
                        Guild server = jda.getGuildById(serverId);
                        if(server != null)
                            serverList.append("> ").append(server.getName()).append("(").append(server.getId()).append(") First Seen: ").append(firstSeen).append("\n");
                    }
                    channel.sendMessage("Servers:\n"+serverList).queue();
                    break;
                case "users":
                    channel.sendMessage("User count: "+returnUserCount()).queue();
                    break;
                default:
                    channel.sendMessage("fuck").queue();
            }
        }
    }

    private PreparedStatement returnAdmins(){
        return Database.prepareStatement("SELECT (DiscordID) FROM users WHERE UserLevel > 2");
    }

    private PreparedStatement returnServers(){
        return Database.prepareStatement("SELECT ServerID, FirstSeen FROM servers");
    }

    private int returnUserCount(){
        return Utilities.getJDAInstance().getUsers().size();
    }

    private void sendStats(MessageChannel channel) {
        JDA jda = Utilities.getJDAInstance();
        EmbedBuilder bd = new EmbedBuilder();
        bd.setTitle("Stats");
        bd.setAuthor(jda.getSelfUser().getName() + " Version "+VERSION, null, jda.getSelfUser().getAvatarUrl());
        bd.setColor(Utilities.getBotColor());
        bd.addField("Uptime", Utilities.getUptimeString(), false);
        bd.addField("Total Users", ""+returnUserCount(), true);
        bd.addField("Total Servers", ""+jda.getGuilds().size(), true);
        channel.sendMessageEmbeds(bd.build()).queue();
    }

}
