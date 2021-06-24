package keith.util;

import keith.managers.ServerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utilities {

    private static long lastReconnect;
    private static JDA jda;


    /*
     * Setter methods for static variables and various features of bot
     */


    public static void updateUptime() {
        lastReconnect = ManagementFactory.getRuntimeMXBean().getUptime();
    }

    public static void setJDA(JDA jda) {
        Utilities.jda = jda;
    }

    public static void setStatus(String newStatus) {
        jda.getPresence().setActivity(Activity.playing(newStatus));
    }

    public static void updateDefaultStatus() {
        jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));
    }


    /*
     * Getter methods for static variables
     */


    public static long getUptimeMillis() {
        return ManagementFactory.getRuntimeMXBean().getUptime() - lastReconnect;
    }

    public static JDA getJDAInstance() {
        return Utilities.jda;
    }

    /*
     * Utility methods
     */

    public static String getUptimeString() {
        long uptime = getUptimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(uptime);
        long hours = TimeUnit.MILLISECONDS.toHours(uptime) % TimeUnit.DAYS.toHours(1);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptime) % TimeUnit.MINUTES.toSeconds(1);
        //Format response, only include larger measurement if relevant.
        String response = "";
        if (days > 0)
            response += (days == 1) ? days+" day, " : days+" days, ";
        if (hours > 0)
            response += (hours == 1) ? hours+" hour, " : hours+" hours, ";
        if (minutes > 0)
            response += (minutes == 1) ? minutes+" minute, " : minutes+" minutes, ";
        if (seconds > 0)
            response += (seconds == 1) ? seconds+" second" : seconds+" seconds";

        return response;
    }

    public static String getPrefix(MessageReceivedEvent event) {
        if (event.getChannel() instanceof PrivateChannel) {
            return "?";
        }
        return ServerManager.getInstance().getServer(event.getGuild().getId()).getPrefix();
    }

    public static String stringListToString(List<String> list) {
        StringBuilder result = new StringBuilder();

        for (String string : list) {
            result.append(string).append("\n");
        }

        return result.toString();
    }

    public static void restart(MessageReceivedEvent event) throws IOException  {
        Process p = Runtime.getRuntime().exec("screen -d -m nohup java -jar /home/succ/keith/v3/build/libs/keithv3-V3.00-all.jar");
        setStatus("Restarting...");
        boolean status = p.isAlive();
        if(status) {
            event.getChannel().sendMessage("Restarting...").queue(success -> System.exit(0));
        } else {
            event.getChannel().sendMessage("Restart failed...").queue();
        }

    }
}
