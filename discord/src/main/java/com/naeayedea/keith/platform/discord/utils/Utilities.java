package com.naeayedea.keith.platform.discord.utils;

import com.naeayedea.keith.core.i18n.TranslationProvider;
import com.naeayedea.keith.core.util.MultiMap;
import com.naeayedea.keith.platform.discord.command.lib.TextCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

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
        jda.getPresence().setActivity(Activity.listening("?help for commands | " + jda.getGuilds().size() + " servers"));
    }

    //update default status if it is already set, otherwise leave current status alone
    public static void updateDefaultStatus() {
        if (Objects.requireNonNull(jda.getPresence().getActivity()).getName().contains("help for commands | ")) {
            jda.getPresence().setActivity(Activity.listening("?help for commands | " + jda.getGuilds().size() + " servers"));  //Default discord status
        }
    }

    /*
     * Getter methods for static variables
     */

    public static long getUptimeMillis() {
        return ManagementFactory.getRuntimeMXBean().getUptime() - lastReconnect;
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
            response += (days == 1) ? days + " day, " : days + " days, ";
        if (hours > 0)
            response += (hours == 1) ? hours + " hour, " : hours + " hours, ";
        if (minutes > 0)
            response += (minutes == 1) ? minutes + " minute, " : minutes + " minutes, ";
        if (seconds > 0)
            response += (seconds == 1) ? seconds + " second" : seconds + " seconds";

        return response;
    }

    public static Color getBotColor() {
        return new Color(155, 0, 155);
    }

    public static Color getDefaultColor() {
        return new Color(44, 47, 51);
    }

    public static Color getMemberColor(Guild guild, User user) {
        Member member = guild.getMember(user);
        if (member != null) {
            List<Role> roles = member.getRoles();
            for (Role role : roles) {
                Color color = role.getColor();
                if (color != null) {
                    return color;
                }
            }
        }
        return new Color(44, 47, 51);
    }


    //Message to retrieve a MessageChannel by its ID when all we need is a message channel regardless of its type
    public static MessageChannel getMessageChannelById(long id) {
        MessageChannel channel = jda.getTextChannelById(id);
        channel = channel != null ? channel : jda.getThreadChannelById(id);
        channel = channel != null ? channel : jda.getPrivateChannelById(id);
        return channel;
    }

    public static MessageChannel getMessageChannelById(String id) {
        MessageChannel channel = jda.getTextChannelById(id);
        channel = channel != null ? channel : jda.getThreadChannelById(id);
        channel = channel != null ? channel : jda.getPrivateChannelById(id);
        return channel;
    }

    public static String readInputStream(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuilder results = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            results.append(inputLine).append("\n");
        }
        //close resources
        in.close();
        return results.toString();
    }

    public static String getImageURL(String html) {
        Pattern pattern = Pattern.compile("(?<=property=\"og:image\" content=\")(\\S+)(\\s*)(?=\")");
        Matcher matcher = pattern.matcher(html);
        String lastMatch = "";
        while (matcher.find()) {
            lastMatch = matcher.group();
        }
        return lastMatch;
    }

    public static String getVideoURL(String html) {
        Pattern pattern = Pattern.compile("(?<=property=\"og:url\" content=\")(\\S+)(\\s*)(?=\")");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static void populateCommandMap(Map<Locale, MultiMap<String, TextCommandHandler>> localeToAliasMap, List<? extends TextCommandHandler> commands, List<String> exclusions, TranslationProvider translationProvider) {
        for (DiscordLocale discordLocale : DiscordLocale.values()) {
            //for every locale in discord
            Locale locale = discordLocale.toLocale();

            MultiMap<String, TextCommandHandler> commandMap = new MultiMap<>();

            //for every command we know of
            for (TextCommandHandler command : commands) {
                if (!exclusions.contains(command.getInternalName())) {
                    //add the aliases
                    commandMap.putAll(Arrays.stream(translationProvider.getTranslation(command.getAliasTranslationKey(), locale).toLowerCase(locale).split(",")).toList(), command);

                    //then add the base name
                    commandMap.put(translationProvider.getTranslation(command.getNameTranslationKey(), locale).toLowerCase(locale), command);
                }
            }

            //add the aliases to the locale
            localeToAliasMap.put(locale, commandMap);
        }
    }

    public static boolean channelIsNSFW(GuildMessageChannelUnion channel) {
        ChannelType type = channel.getType();

        switch (type) {
            case TEXT -> {
                return channel.asTextChannel().isNSFW();
            }
            case VOICE -> {
                return channel.asVoiceChannel().isNSFW();
            }
            case NEWS -> {
                return channel.asNewsChannel().isNSFW();
            }
            case STAGE -> {
                return channel.asStageChannel().isNSFW();
            }
            case GUILD_NEWS_THREAD -> {
                return channel.asThreadChannel().getParentChannel().asNewsChannel().isNSFW();
            }
            case GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD -> {
                return channel.asThreadChannel().getParentChannel().asTextChannel().isNSFW();
            }
            default -> {
                return false;
            }
        }
    }
}
