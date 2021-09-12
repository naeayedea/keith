package keith.util;

import keith.managers.ServerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Utilities {


    public static class Messages {

        public static void sendError(MessageChannel channel, String header, String message) {
            channel.sendMessageEmbeds(MessageTemplate.errorMessage(header, message).build()).queue();
        }

        public static class MessageTemplate {
            /*
             * Various message templates for ease of use when sending user a message such as error messages, information etc.
             * Add various message types as required
             */

            public static EmbedBuilder errorMessage(String header, String text) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("ERROR");
                eb.setDescription(header);
                eb.addField("Information", text, false);
                eb.setColor(new Color (155,0,0));
                return eb;
            }

        }

        public static void sendEmbed(String channelID, String title, String message, MessageReceivedEvent event){
            try{
                TextChannel channel = jda.getTextChannelById(channelID);
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(title);
                eb.setDescription(message);
                eb.setColor(new Color(155,0,155));
                if (channel != null) {
                    channel.sendMessageEmbeds(eb.build()).queue();
                } else {
                    event.getChannel().sendMessage("Channel Unavailable, check id or for voice").queue();
                }
            }
            catch (IllegalArgumentException e){
                event.getChannel().sendMessage("Message send error").queue();
            }
        }

        public static void sendMessage(String channelID, String message, MessageReceivedEvent event ){
            try {
                TextChannel channel = jda.getTextChannelById(channelID);
                if (channel != null) {
                    channel.sendMessage(message).queue();
                } else {
                    event.getChannel().sendMessage("Channel Unavailable, check id or for voice").queue();
                }
            } catch (IllegalArgumentException e){
                event.getChannel().sendMessage("Message send error").queue();
            }
        }
    }

    private static long lastReconnect;
    private static JDA jda;

    /*stores the maximum total "cost" of commands a user can do within 30 seconds, the cost of each command is defined
     * individually depending on the amount of time it takes to complete on average */
    private static int rateLimitMax;

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

    public static void setRateLimitMax(int newMax) {
        rateLimitMax = newMax;
    }

    //set status to default by force
    public static void forceDefaultStatus() {
        jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));
    }

    //update default status if it is already set, otherwise leave current status alone
    public static void updateDefaultStatus() {
        if(Objects.requireNonNull(jda.getPresence().getActivity()).getName().contains("help for commands | ")){
            jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));  //Default discord status
        }
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

    public static int getRateLimitMax() {
        return rateLimitMax;
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

    public static Color getBotColor() {
        return new Color(155,0,155);
    }

    public static Color getDefaultColor() {
        return new Color(44,47,51);
    }

    public static Color getMemberColor(Guild guild, User user) {
            Member member = guild.getMember(user);
            if (member != null) {
                List<Role> roles = member.getRoles();
                for (Role role : roles) {
                    Color color = role.getColor();
                    if(color != null) {
                        return color;
                    }
                }
            }
            return new Color(44,47,51);
    }

    public static Color getColorFromString(String string) {
        String stringHash;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(string.getBytes());
            stringHash = new String(messageDigest.digest());

        } catch (NoSuchAlgorithmException e) {
            //if for some reason SHA-256 was to disappear, just revert to regular old string.getHash()
            stringHash = string;
        }
        int pv = 0xFFFFFF & stringHash.hashCode();
        int R, G, B;
        R = pv & 255; G = (pv >> 8) & 255; B = (pv >> 16) & 255;
        return new Color(R,G,B);
    }

    public static Color getRandomColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new Color(random.nextInt(0, 255+1), random.nextInt(0, 255+1), random.nextInt(0, 255+1));
    }

    //rebuilds a string list into a "sentence" by appending spaces
    public static String stringListToString(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (String string : list) {
            result.append(string).append(" ");
        }
        return result.toString().trim();
    }

    public static void restart(MessageReceivedEvent event) {
        try {
            Message message = event.getChannel().sendMessage("Restarting...").complete();
            Process p = Runtime.getRuntime().exec("screen -dm  java -jar /home/succ/keith/v3/build/libs/keithv3-v3.00-all.jar " + message.getId() + " " + message.getChannel().getId());
            setStatus("Restarting...");
            try {
                if (p.waitFor(10, TimeUnit.SECONDS)) {
                    System.exit(0);
                } else {
                    event.getChannel().sendMessage("Restart failed...").queue();
                }
            } catch (InterruptedException ignored) {}

        } catch (IOException e) {
            event.getChannel().sendMessage("Restart failed badly...").queue();
        }

    }

    public static String truncateString(String string, int length) {
        if (length > 0) {
            String format = "%-"+ length+"s";
            String result = String.format(format, string);
            if (string.length() > length) {
                result = result.substring(0, length - 2) + "..";
            }
            return result;
        }
        return "";
    }
}
