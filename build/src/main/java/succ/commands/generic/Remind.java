package succ.commands.generic;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.Database;
import succ.util.logs.ConsoleLogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Remind extends UserCommand{

    private Database database;
    private JDA jda;
    private ScheduledThreadPoolExecutor executor;
    private ConsoleLogger logger;
    String inTimeRegex = "((^in)( )+[0-9]+( )*(month(s)?)){0,1}((^in )?(,( )*)?[0-9]+( )*(week(s)?)){0,1}((^in )?(,( )*)?[0-9]+( )*(day(s)?)){0,1}((^in )?(,( )*)?[0-9]+( )*(hour(s)?)){0,1}((^in )?(,( )*)?[0-9]+( )*(minute(s)?)){0,1}((^in )?(,( )*)?[0-9]+( )*(second(s)?)){0,1}";
    String onDateRegex= "^(^(on) \\d{1,2}\\/\\d{1,2}\\/\\d{4}$|(^(on) \\d{1,2}-\\d{1,2}-\\d{4}$)|(^(on) \\d{1,2} \\d{1,2} \\d{4}$)){1}";
    String[] dateFormats = {"d-M-y","d/M/y","d M y"};

    public Remind(Database database, JDA jda){
        this.database = database;
        this.jda = jda;
        executor = new ScheduledThreadPoolExecutor(200);
        logger = new ConsoleLogger();
        loadReminders();
    }
    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "remind: \"sets a reminder for the specified time/date\"";
    }

    //?remind [in days]/[on date] [text]
    @Override
    public void run(MessageReceivedEvent event) {
        String commandRaw = event.getMessage().getContentRaw().trim();
        String args[]=commandRaw.split("\\s+");
        if(args.length<4){
            event.getChannel().sendMessage("Insufficient Arguments").queue();
            return;
        }
        commandRaw=commandRaw.substring(args[0].length()+1);
        System.out.println(commandRaw);
        Pattern in = Pattern.compile(inTimeRegex);
        Pattern on = Pattern.compile(onDateRegex);
        Pattern num = Pattern.compile("\\d+");
        Matcher inMatcher=in.matcher(commandRaw);
        Matcher onMatcher=on.matcher(commandRaw);
        Matcher numMatcher;
        if(inMatcher.find()){
            String inString = inMatcher.group();
            System.out.println("inString: "+inString);
            int n =0;
            long[] quantifiers = {0,0,0,0,0,0}; //months,weeks,days,hours,minutes,seconds
            numMatcher=num.matcher(inString);
            while(numMatcher.find()){
                quantifiers[n]=Long.parseLong(numMatcher.group());
                n++;
            }
            for(long thing : quantifiers){
                System.out.println(thing);
            }
            long time=System.currentTimeMillis()
                     +TimeUnit.SECONDS.toMillis(quantifiers[5])
                     +TimeUnit.MINUTES.toMillis(quantifiers[4])
                     +TimeUnit.HOURS.toMillis(quantifiers[3])
                     +TimeUnit.DAYS.toMillis(quantifiers[2])
                     +TimeUnit.DAYS.toMillis(quantifiers[1]*7)
                     +TimeUnit.DAYS.toMillis(quantifiers[0]*29);
            String text = commandRaw.substring(inString.length()+1);
            System.out.println("text "+text);
            String guildid = event.getGuild().getId();
            System.out.println("guild "+guildid);
            String channelid = event.getChannel().getId();
            System.out.println("channel "+channelid);
            String userid = event.getAuthor().getId();
            System.out.println("user "+userid);
            Runnable remind = ()->{
                TextChannel channel = jda.getTextChannelById(channelid);
                System.out.println(channel.toString());
                User user = jda.getUserById(userid);
                System.out.println(user.toString());
                if(channel==null){
                    channel=jda.getGuildById(guildid).getDefaultChannel();
                    if(channel==null){
                        if(user==null){
                            logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                            removeReminder(guildid,channelid,userid,time,text);
                            return;
                        }
                        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(user.getAsMention()+" You asked me to remind you of this: "+text)).queue();
                        removeReminder(guildid,channelid,userid,time,text);
                        return;
                    }
                }
                if(user==null){
                    logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                    removeReminder(guildid,channelid,userid,time,text);
                    return;
                }
                removeReminder(guildid,channelid,userid,time,text);
                channel.sendMessage(user.getAsMention()+" You asked me to remind you of this: ```"+text+"```").queue();
            };
            executor.schedule(remind,time-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            database.setReminder(guildid, channelid,userid,time,text);
            event.getChannel().sendMessage("Reminding you in "+inString+": "+text).queue();
            return;
        }
        if(onMatcher.find()){
            String onString = onMatcher.group();
            Date date = parseDate(onString);
            if(date==null){
                event.getChannel().sendMessage("Couldn't set reminder").queue();
                return;
            }
            long time = date.getTime();
            String text = commandRaw.substring(commandRaw.indexOf(onString));
            String guildid = event.getGuild().getId();
            String channelid = event.getChannel().getId();
            String userid = event.getAuthor().getId();
            Runnable remind = ()->{
                TextChannel channel = jda.getTextChannelById(channelid);
                User user = jda.getUserById(userid);
                if(channel==null){
                    channel=jda.getGuildById(guildid).getDefaultChannel();
                    if(channel==null){
                        if(user==null){
                            System.out.println("here1");
                            logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                            removeReminder(guildid,channelid,userid,time,text);
                            return;
                        }
                        System.out.println("here2");
                        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(user.getAsMention()+" You asked me to remind you of this: "+text)).queue();
                        removeReminder(guildid,channelid,userid,time,text);
                        return;
                    }
                }
                if(user==null){
                    System.out.println("here3");
                    logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                    removeReminder(guildid,channelid,userid,time,text);
                    return;
                }
                System.out.println("here4");
                removeReminder(guildid, channelid,userid,time,text);
                channel.sendMessage(user.getAsMention()+" You asked me to remind you of this: ```"+text+"```").queue();
            };
            executor.schedule(remind,time-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            database.setReminder(guildid, channelid,userid,time,text);
            event.getChannel().sendMessage("Reminding you on "+onString+": "+text).queue();
            return;
        }
        event.getChannel().sendMessage("Couldn't set reminder, try use dd/mm/yyyy").queue();
    }

    //Loads reminders for next 10 days
    public void loadReminders(){
        System.out.println("loading");
        executor.schedule(()->{loadReminders();logger.printSuccess("loading more reminders");},9, TimeUnit.DAYS);
        long tenDaysLater=System.currentTimeMillis()+TimeUnit.DAYS.toMillis(10);
        ArrayList<String> results = database.query("SELECT guildid, channelid, userid, date, text FROM reminders WHERE date < "+tenDaysLater);
        String guildid;
        String channelid;
        String userid;
        long time;
        String text;
        for(int i=1; i<results.size();i++){
            String result = results.get(i);
            System.out.println(result);
            String args[]=result.split("\\s+");
            guildid=args[0];
            channelid=args[1];
            userid=args[2];
            time=Long.parseLong(args[3]);
            System.out.println(time);
            text=result.substring(result.indexOf(args[3])+args[3].length()+2);
            System.out.println(text);
            Runnable remind = ()->{
                TextChannel channel = jda.getTextChannelById(channelid);
                User user = jda.getUserById(userid);
                if(channel==null){
                    channel=jda.getGuildById(guildid).getDefaultChannel();
                    if(channel==null){
                        if(user==null){
                            System.out.println("here1");
                            logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                            removeReminder(guildid,channelid,userid,time,text);
                            return;
                        }
                        System.out.println("here2");
                        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(user.getAsMention()+" You asked me to remind you of this: "+text)).queue();
                        removeReminder(guildid,channelid,userid,time,text);
                        return;
                    }
                }
                if(user==null){
                    System.out.println("here3");
                    logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                    removeReminder(guildid,channelid,userid,time,text);
                    return;
                }
                System.out.println("here4");
                removeReminder(guildid, channelid,userid,time,text);
                channel.sendMessage(user.getAsMention()+" You asked me to remind you of this: ```"+text+"```").queue();
            };
            executor.schedule(remind,time-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            return;
        }
    }

    private void removeReminder(String guildid, String channelid, String userid, long date, String text){
        System.out.println("removing");
        System.out.println(guildid);
        System.out.println(channelid);
        System.out.println(userid);
        System.out.println(date);
        System.out.println(text);
        database.query("DELETE FROM reminders WHERE ("
                                +"guildid='"+guildid
                                +"' AND channelid='"+channelid
                                +"' AND userid='"+userid
                                +"' AND date="+date
                                +" AND text='"+text
                                +"')");
    }

    private Date parseDate(String dateString){
        for(String format : dateFormats){
            try{
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                java.util.Date date = dateFormat.parse(dateString);
                return date;
            }
            catch (ParseException e) {}
        }
        return null;
    }
}
