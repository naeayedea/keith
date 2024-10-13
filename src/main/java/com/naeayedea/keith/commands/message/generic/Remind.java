package com.naeayedea.keith.commands.message.generic;

import com.naeayedea.keith.util.Database;
import com.naeayedea.keith.util.Utilities;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    private static class RemindExecutor extends ScheduledThreadPoolExecutor {

        /**
         * RemindExecutor is an executor where all remaining tasks can be easily cancelled, does this by maintaining list
         * of all scheduledfutures for easy access later. cleanup() should be called occasionally to clear finished futures.
         */
        private final List<ScheduledFuture<?>> tasks;

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
                future.cancel(true);
            }

            tasks.clear();
        }

        public void cleanup() {
            tasks.removeIf(Future::isDone);
        }
    }

    @Value("${keith.commands.remind.statements.getReminders}")
    private String GET_REMINDERS_STATEMENT;

    @Value("${keith.commands.remind.statements.removeReminder}")
    private String REMOVE_REMINDER_STATEMENT;

    @Value("${keith.commands.remind.statements.setReminder}")
    private String SET_REMINDER_STATEMENT;

    @Value("${keith.executor.reminders.scheduler.poolSize.core}")
    private int corePoolSize;

    private final Database database;

    private final String onDateRegex = "(?<=on)( )*(\\d{1,2}/\\d{1,2}/\\d{4}|(\\d{1,2}-\\d{1,2}-\\d{4})|(\\d{1,2} \\d{1,2} \\d{4}))";

    private final String[] dateFormats = {"d-M-y", "d/M/y", "d M y"};

    private final RemindExecutor executor;

    private final Logger logger = LoggerFactory.getLogger(Remind.class);

    public Remind(@Value("${keith.commands.remind.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.remind.aliases}', ',')}") List<String> commandAliases, Database database) {
        super(defaultName, commandAliases);

        this.database = database;
        this.executor = new RemindExecutor(corePoolSize);

        executor.scheduleAtFixedRate(executor::cleanup, 12, 12, TimeUnit.HOURS);

    }

    @PostConstruct
    public void loadReminders() {
        executor.clear();

        executor.schedule(() -> {
            loadReminders();
            logger.info("loading more reminders");
        }, 10, TimeUnit.DAYS);

        long tenDaysLater = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10);

        List<String> results = database.getStringResult(GET_REMINDERS_STATEMENT, tenDaysLater);

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
                executor.schedule(remind, time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            }
            n++;
        }
        logger.info("Reloaded {} reminders", n - 1);
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix + getDefaultName() + ": \"set a reminder and the bot will message you after the specified timeframe!\"";
    }

    @Override
    public String getLongDescription() {
        return """
            Forgetful? Use remind to have the bot tag you in the specified amount of time\
            with a message to help you remember! Acceptable uses are:
            "remind in X months, Y days, Z hours [message]" etc. you do not need to include all times so "remind in X hours [message]" will also work!
            "remind on [date] at [time] [message]" - please use format dd/mm/yyyy, dd-mm-yyyy or dd mm yyyy, it is not necessary to specify a time
            
            please note that reminders cannot be empty e.g. you must have something to be reminded of!""";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        if (tokens.size() < 2) {
            event.getChannel().sendMessage("Insufficient Arguments").queue();
            return;
        }

        String commandRaw = Utilities.stringListToString(tokens);


        String inTimeRegex = "((^in)( )+[0-9]+( )*(month(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(week(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(day(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(hour(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(minute(s)?))?((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(second(s)?))?";

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
        } else if (onMatcher.find()) {
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

        if (inString.isEmpty()) {
            return handleOn(event, commandRaw);

        }
        long[] quantifiers = {0, 0, 0, 0, 0, 0}; //months,weeks,days,hours,minutes,seconds

        Matcher numMatcher = num.matcher(inString);

        String copy = inString;
        while (numMatcher.find()) {
            try {
                //Not proud of this one, will change regex later
                if (copy.contains("month")) {
                    quantifiers[0] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("month", "");
                } else if (copy.contains("week")) {
                    quantifiers[1] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("week", "");
                } else if (copy.contains("day")) {
                    quantifiers[2] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("day", "");
                } else if (copy.contains("hour")) {
                    quantifiers[3] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("hour", "");
                } else if (copy.contains("minute")) {
                    quantifiers[4] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("minute", "");
                } else if (copy.contains("second")) {
                    quantifiers[5] = Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("second", "");
                }
            } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
            }
        }

        long time = System.currentTimeMillis()
            + TimeUnit.SECONDS.toMillis(quantifiers[5])
            + TimeUnit.MINUTES.toMillis(quantifiers[4])
            + TimeUnit.HOURS.toMillis(quantifiers[3])
            + TimeUnit.DAYS.toMillis(quantifiers[2])
            + TimeUnit.DAYS.toMillis(quantifiers[1] * 7)
            + TimeUnit.DAYS.toMillis(quantifiers[0] * 29);

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
                text = " ";
            }
        }

        createReminder(event, inString, time, text);

        return true;
    }

    private boolean handleOn(MessageReceivedEvent event, String commandRaw) {
        Pattern on = Pattern.compile(onDateRegex);

        String timeRegex = "(?<=at)( )*([0-9]{1,2}:[0-9]{1,2})?";

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
                time += TimeUnit.HOURS.toMillis(Integer.parseInt(times[0])) + TimeUnit.MINUTES.toMillis(Integer.parseInt(times[1]));

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
                    text = " ";
                }
            }

            if (time < 0) {
                event.getChannel().sendMessage("That is a long way away! Ask me to closer to the time").queue();
                return false;
            }

            createReminder(event, commandRaw.substring(0, index).trim(), time, text);

            return true;
        }

        return false;
    }


    private void createReminder(MessageReceivedEvent event, String onString, long time, String text) {
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

            return;
        }

        executor.schedule(remind, time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        database.executeUpdate(SET_REMINDER_STATEMENT, guildId, channelId, userid, time, text);

        event.getChannel().sendMessage("Reminding you " + onString + ":\n> " + text).queue();
    }

    private void remind(String guildId, String channelId, String userId, long time, String text, JDA jda, MessageChannel channel) {
        User user = jda.getUserById(userId);

        //Check if the original message channel still exists
        if (channel == null) {
            //if it doesn't, make sure the user still exists, and if they do try and send a private message of the reminder
            if (user == null) {
                logger.warn(getCantSetReminderLogMessage(), userId, guildId);
            } else {
                user.openPrivateChannel().flatMap(privateChannel -> {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Reminder");
                    embed.setColor(Utilities.getColorFromString(text));
                    embed.setDescription(text);
                    return privateChannel.sendMessage(user.getAsMention() + " You asked me to remind you of this:").setEmbeds(embed.build());
                }).queue();
            }
            database.executeUpdate(GET_REMINDERS_STATEMENT, guildId, channelId, userId, time, text);
        } else {
            if (user == null) {
                logger.warn(getCantSetReminderLogMessage(), userId, guildId);

                database.executeUpdate(REMOVE_REMINDER_STATEMENT, guildId, channelId, userId, time, text);
                return;
            }

            database.executeUpdate(REMOVE_REMINDER_STATEMENT, guildId, channelId, userId, time, text);

            sendEmbed(channel, user.getAsMention() + " You asked me to remind you of this:", text);
        }
    }

    private String getCantSetReminderLogMessage() {
        return "couldn't send reminder to user {} at guild {}";
    }

    public void sendEmbed(MessageChannel channel, String mention, String text) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Reminder");
        embed.setColor(Utilities.getColorFromString(text));
        embed.setDescription(text);
        channel.sendMessage(mention).setEmbeds(embed.build()).queue();
    }

    private Date parseDate(String dateString) {
        for (String format : dateFormats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
                return dateFormat.parse(dateString);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}
