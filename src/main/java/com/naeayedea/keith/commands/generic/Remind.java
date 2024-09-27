package com.naeayedea.keith.commands.generic;

import com.naeayedea.keith.util.Database;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Remind extends AbstractUserCommand {

    public static class RemindExecutor extends ScheduledThreadPoolExecutor {

        /**
         * RemindExecutor is an executor where all remaining tasks can be easily cancelled, does this by maintaining list
         * of all scheduledfutures for easy access later. cleanup() should be called occasionally to clear finished futures.
         */
        List<ScheduledFuture<?>> tasks;

        public RemindExecutor(int corePoolSize) {
            super(corePoolSize);
            tasks = new ArrayList<>();
        }

        @NotNull
        @Override
        public ScheduledFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
            ScheduledFuture<?> future = super.schedule(command, delay, unit);
            tasks.add(future);
            return future;
        }

        public void clear() {
            for (ScheduledFuture<?> future : tasks) {
                future.cancel(false);
            }
            tasks.clear();
        }

        public void cleanup() {
            tasks.removeIf(Future::isDone);
        }
    }

    String inTimeRegex = "((^in)( )+[0-9]+( )*(month(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(week(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(day(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(hour(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(minute(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(second(s)?))?";
    String onDateRegex = "(?<=on)( )*(\\d{1,2}/\\d{1,2}/\\d{4}|(\\d{1,2}-\\d{1,2}-\\d{4})|(\\d{1,2} \\d{1,2} \\d{4}))";
    String timeRegex = "(?<=at)( )*([0-9]{1,2}:[0-9]{1,2})?";
    String[] dateFormats = {"d-M-y","d/M/y","d M y"};
    RemindExecutor executor;

    private final Logger logger = LoggerFactory.getLogger(Remind.class);

    public Remind(@Value("${keith.commands.remind.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.remind.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);

        this.executor = new RemindExecutor(200);

        executor.scheduleAtFixedRate(executor::cleanup, 12, 12, TimeUnit.HOURS);

        loadReminders();
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"set a reminder and the bot will message you after the specified timeframe!\"";
    }

    @Override
    public String getLongDescription() {
        return "Forgetful? Use remind to have the bot tag you in the specified amount of time" +
                "with a message to help you remember! Acceptable uses are:\n" +
                "\"remind in X months, Y days, Z hours [message]\" etc. you do not need to include all times so \"remind in X hours [message]\" will also work!\n" +
                "\"remind on [date] at [time] [message]\" - please use format dd/mm/yyyy, dd-mm-yyyy or dd mm yyyy, it is not necessary to specify a time\n\n" +
                "please note that reminders cannot be empty e.g. you must have something to be reminded of!";
    }

    private PreparedStatement getRemindersStatement() {
        return Database.prepareStatement("SELECT GuildID, ChannelID, UserID, Date, Text FROM reminders WHERE date < ?");
    }

    private PreparedStatement removeReminderStatement() {
        return Database.prepareStatement("DELETE FROM reminders WHERE (GuildID = ? AND ChannelID = ? AND UserID = ? AND Date = ? AND text = ?)");
    }

    private PreparedStatement setReminderStatement() {
        return Database.prepareStatement("INSERT INTO reminders (GuildID, ChannelID, UserID, Date, Text) VALUES (?, ?, ?, ?, ?)");
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        if(tokens.size() < 2){
            event.getChannel().sendMessage("Insufficient Arguments").queue();
            return;
        }
        String commandRaw = Utilities.stringListToString(tokens);
        Pattern in = Pattern.compile(inTimeRegex);
        Pattern on = Pattern.compile(onDateRegex);
        Matcher inMatcher = in.matcher(commandRaw);
        Matcher onMatcher = on.matcher(commandRaw);
        if (inMatcher.find()) {
            if (!handleIn(event, commandRaw, inMatcher)) {
                event.getChannel().sendMessage("Couldn't set reminder").queue();
                return;
            }
            return;
        }
        else if (onMatcher.find()) {
            if (!handleOn(event, commandRaw)) {
                event.getChannel().sendMessage("Couldn't set reminder").queue();
                return;
            }
            return;
        }
        event.getChannel().sendMessage("Couldn't set reminder, try use dd/mm/yyyy").queue();
    }

    private boolean handleIn(MessageReceivedEvent event, String commandRaw, Matcher inMatcher) {
        String inString = inMatcher.group();
        Pattern num = Pattern.compile("\\d+");
        if (inString.equals("")) {
            return handleOn(event,commandRaw);

        }
        long[] quantifiers = {0,0,0,0,0,0}; //months,weeks,days,hours,minutes,seconds
        Matcher numMatcher = num.matcher(inString);
        String copy = inString;
        while (numMatcher.find()) {
            try{
                //Not proud of this one, will change regex later
                if (copy.contains("month")) {
                    quantifiers[0] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("month","");
                } else if (copy.contains("week")) {
                    quantifiers[1] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("week","");
                } else if (copy.contains("day")) {
                    quantifiers[2] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("day","");
                } else if (copy.contains("hour")) {
                    quantifiers[3] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("hour","");
                } else if (copy.contains("minute")) {
                    quantifiers[4] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("minute","");
                } else if (copy.contains("second")) {
                    quantifiers[5] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("second","");
                }
            }
            catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {}
        }
        long time=System.currentTimeMillis()
                + TimeUnit.SECONDS.toMillis(quantifiers[5])
                + TimeUnit.MINUTES.toMillis(quantifiers[4])
                + TimeUnit.HOURS.toMillis(quantifiers[3])
                + TimeUnit.DAYS.toMillis(quantifiers[2])
                + TimeUnit.DAYS.toMillis(quantifiers[1]*7)
                + TimeUnit.DAYS.toMillis(quantifiers[0]*29);
        if (time < 0) {
            event.getChannel().sendMessage("That is a long way away! Ask me to closer to the time").queue();
            return false;
        }
        String text;
        Message commandMessage = event.getMessage();
        if (commandMessage.getType().equals(MessageType.INLINE_REPLY)) {
            //use the text of the message being replied to
            Message content = commandMessage.getReferencedMessage();
            if (content != null) {
                text = content.getContentRaw().trim();
            } else {
                event.getChannel().sendMessage("Couldn't retrieve message").queue();
                return false;
            }
        } else {
            //use the text of the command message
            if (commandRaw.length() > inString.length() + 1) {
                text = commandRaw.substring(inString.length() + 1);
            } else {
                text= " ";
            }
        }
        return createReminder(event, inString, time, text);
    }
    private boolean handleOn(MessageReceivedEvent event, String commandRaw) {
        Pattern on = Pattern.compile(onDateRegex);
        Pattern at = Pattern.compile(timeRegex);
        Matcher onMatcher = on.matcher(commandRaw);
        Matcher atMatcher = at.matcher(commandRaw);
        if (onMatcher.find()) {
            String onString = onMatcher.group();
            Date date = parseDate(onString);
            if (date == null) {
                return false;
            }
            long time = date.getTime();

            String text;
            int index;
            if (commandRaw.contains("at") && atMatcher.find()) {
                String atString = atMatcher.group();
                String[] times = atString.trim().split(":");
                //shouldn't need to wrap below in try/catch as timeRegex only matches two integers surrounding ':'
                long offset = TimeUnit.HOURS.toMillis(Integer.parseInt(times[0]))+ TimeUnit.MINUTES.toMillis(Integer.parseInt(times[1]));
                time += offset;
                index = commandRaw.indexOf(atString) + atString.length();
            } else {
                index = commandRaw.indexOf(onString) + onString.length();

            }
            Message commandMessage = event.getMessage();
            if (commandMessage.getType().equals(MessageType.INLINE_REPLY)) {
                //use the text of the message being replied to
                Message content = commandMessage.getReferencedMessage();
                if (content != null) {
                    text = content.getContentRaw().trim();
                } else {
                    event.getChannel().sendMessage("Couldn't retrieve message").queue();
                    return false;
                }
            } else {
                if (commandRaw.length() > index) {
                    text = commandRaw.substring(index).trim();
                } else {
                    text= " ";
                }
            }
            if (time < 0) {
                event.getChannel().sendMessage("That is a long way away! Ask me to closer to the time").queue();
                return false;
            }
            return createReminder(event, commandRaw.substring(0, index).trim(), time, text);
        }
        return false;
    }


    private boolean createReminder(MessageReceivedEvent event, String onString, long time, String text) {
        String guildId;
        if (event.getChannel() instanceof PrivateChannel) {
            guildId = "";
        } else {
            guildId = event.getGuild().getId();
        }
        String channelId = event.getChannel().getId();
        String userid = event.getAuthor().getId();
        Runnable remind = () -> {
            JDA jda = Utilities.getJDAInstance();
            MessageChannel channel = Utilities.getMessageChannelById(channelId);
            remind(guildId, channelId, userid, time, text, jda, channel);
        };
        if (text.trim().isEmpty()) {
            event.getChannel().sendMessage("Reminders can't be empty else you might forget what it was!").queue();
            return true;
        }
        executor.schedule(remind,time-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        Database.executeUpdate(setReminderStatement(), guildId, channelId,userid,time,text);
        event.getChannel().sendMessage("Reminding you "+onString+":\n> "+text).queue();
        return true;
    }

    private void remind(String guildId, String channelId, String userId, long time, String text, JDA jda, MessageChannel channel) {
        User user = jda.getUserById(userId);
        //Check if the original message channel still exists
        if (channel == null) {
            //if it doesn't, make sure the user still exists, and if they do try and send a private message of the reminder
            if (user == null) {
                logger.warn("couldn't send reminder to user "+userId+" at guild "+guildId);
            } else {
                user.openPrivateChannel().flatMap(privateChannel -> {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Reminder");
                    embed.setColor(Utilities.getColorFromString(text));
                    embed.setDescription(text);
                    return privateChannel.sendMessage(user.getAsMention()+" You asked me to remind you of this:").setEmbeds(embed.build());
                }).queue();
            }
            Database.executeUpdate(removeReminderStatement(), guildId,channelId,userId,time,text);
        } else {
            if (user == null) {
                logger.warn("couldn't send reminder to user "+userId+" at guild "+guildId);
                Database.executeUpdate(removeReminderStatement(), guildId,channelId,userId,time,text);
                return;
            }
            Database.executeUpdate(removeReminderStatement(), guildId, channelId, userId, time, text);
            sendEmbed(channel,user.getAsMention()+" You asked me to remind you of this:", text);
        }
    }

    public void sendEmbed(MessageChannel channel, String mention, String text) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Reminder");
        embed.setColor(Utilities.getColorFromString(text));
        embed.setDescription(text);
        channel.sendMessage(mention).setEmbeds(embed.build()).queue();
    }

    public void loadReminders() {
        executor.clear();
        executor.schedule(() -> {loadReminders();logger.info("loading more reminders");},10, TimeUnit.DAYS);
        long tenDaysLater = System.currentTimeMillis()+TimeUnit.DAYS.toMillis(10);
        ArrayList<String> results = Database.getStringResult(getRemindersStatement(), tenDaysLater);
        int n = 0;
        for (String result : results) {
            if (n != 0) {
                String guildId;
                String channelId;
                String userid;
                long time;
                String text;
                String[] args = result.split("\\s+");
                guildId = args[0];
                channelId = args[1];
                userid = args[2];
                time = Long.parseLong(args[3]);
                text = result.substring(result.indexOf(args[3]) + args[3].length() + 1).trim();
                Runnable remind = () -> {
                    JDA jda = Utilities.getJDAInstance();
                    TextChannel channel = Utilities.getJDAInstance().getTextChannelById(channelId);
                    remind(guildId, channelId, userid, time, text, jda, channel);
                };
                executor.schedule(remind,time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                }
            n++;
        }
        logger.info("Reloaded {} reminders", n - 1);
    }

    private Date parseDate(String dateString) {
        for (String format : dateFormats) {
            try{
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
                return dateFormat.parse(dateString);
            }
            catch (ParseException ignored) {}
        }
        return null;
    }
}
