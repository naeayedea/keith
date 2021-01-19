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
    String inTimeRegex = "((^in)( )+[0-9]+( )*(month(s)?)){0,1}((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(week(s)?)){0,1}((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(day(s)?)){0,1}((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(hour(s)?)){0,1}((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(minute(s)?)){0,1}((^in )?((,)?(( )*and)?( )*)?[0-9]+( )*(second(s)?)){0,1}";
    String onDateRegex= "(?<=on( )?)(\\d{1,2}\\/\\d{1,2}\\/\\d{4}|(\\d{1,2}-\\d{1,2}-\\d{4})|(\\d{1,2} \\d{1,2} \\d{4}))";
    String[] dateFormats = {"d-M-y","d/M/y","d M y"};

    public Remind(Database database, JDA jda){
        this.database = database;
        this.jda = jda;
        executor = new ScheduledThreadPoolExecutor(200);
        logger = new ConsoleLogger();
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
        Pattern in = Pattern.compile(inTimeRegex);
        Pattern on = Pattern.compile(onDateRegex);
        Pattern num = Pattern.compile("\\d+");
        Matcher inMatcher=in.matcher(commandRaw);
        Matcher onMatcher=on.matcher(commandRaw);
        Matcher numMatcher;
        if(inMatcher.find()){
            if(!handleIn(event, commandRaw, inMatcher)){
                event.getChannel().sendMessage("Couldn't set reminder").queue();
                return;
            }
            return;
        }
        else if(onMatcher.find()){
            if(!handleOn(event, commandRaw)){
                event.getChannel().sendMessage("Couldn't set reminder").queue();
                return;
            }
            return;
        }
        event.getChannel().sendMessage("Couldn't set reminder, try use dd/mm/yyyy").queue();
    }

    //Loads reminders for next 10 days
    public void loadReminders(){
        executor.schedule(()->{loadReminders();logger.printSuccess("loading more reminders");},10, TimeUnit.DAYS);
        long tenDaysLater=System.currentTimeMillis()+TimeUnit.DAYS.toMillis(10);
        ArrayList<String> results = database.query("SELECT guildid, channelid, userid, date, text FROM reminders WHERE date < "+tenDaysLater);
        int n=0;
        for(int i=1; i<results.size();i++){
            String guildid;
            String channelid;
            String userid;
            long time;
            String text;
            String result = results.get(i);
            String args[]=result.split("\\s+");
            guildid=args[0];
            channelid=args[1];
            userid=args[2];
            time=Long.parseLong(args[3]);
            text=result.substring(result.indexOf(args[3])+args[3].length()+1).trim();
            Runnable remind = ()->{
                TextChannel channel = jda.getTextChannelById(channelid);
                User user = jda.getUserById(userid);
                if(channel==null){
                    channel=jda.getGuildById(guildid).getDefaultChannel();
                    if(channel==null){
                        if(user==null){
                            logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                            database.removeReminder(guildid,channelid,userid,time,text);
                            return;
                        }
                        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(user.getAsMention()+" You asked me to remind you of this: "+text)).queue();
                        database.removeReminder(guildid,channelid,userid,time,text);
                        return;
                    }
                }
                if(user==null){
                    logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                    database.removeReminder(guildid,channelid,userid,time,text);
                    return;
                }
                database.removeReminder(guildid, channelid,userid,time,text);
                channel.sendMessage(user.getAsMention()+" You asked me to remind you of this: ```"+text+"```").queue();
            };
            executor.schedule(remind,time-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            n++;
        }
        logger.printSuccess("Reloaded "+n+" reminders");
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

    private boolean handleIn(MessageReceivedEvent event, String commandRaw, Matcher inMatcher){
        String inString = inMatcher.group();
        Pattern num = Pattern.compile("\\d+");
        if(inString.equals("")){
            return handleOn(event,commandRaw);

        }
        long[] quantifiers = {0,0,0,0,0,0}; //months,weeks,days,hours,minutes,seconds
        Matcher numMatcher=num.matcher(inString);
        String copy = inString;
        while(numMatcher.find()){
            try{
                //Not proud of this one, will change regex later
                if(copy.contains("month")){
                    quantifiers[0]=Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("month","");
                }else if(copy.contains("week")){
                    quantifiers[1]=Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("week","");
                }else if(copy.contains("day")){
                    quantifiers[2]=Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("day","");
                }else if(copy.contains("hour")){
                    quantifiers[3]=Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("hour","");
                }else if(copy.contains("minute")){
                    quantifiers[4]=Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("minute","");
                }else if(copy.contains("second")){
                    quantifiers[5]=Long.parseLong(numMatcher.group());
                    copy = copy.replaceAll("second","");
                }
            }
            catch(NumberFormatException | StringIndexOutOfBoundsException e){
            }
        }
        long time=System.currentTimeMillis()
                +TimeUnit.SECONDS.toMillis(quantifiers[5])
                +TimeUnit.MINUTES.toMillis(quantifiers[4])
                +TimeUnit.HOURS.toMillis(quantifiers[3])
                +TimeUnit.DAYS.toMillis(quantifiers[2])
                +TimeUnit.DAYS.toMillis(quantifiers[1]*7)
                +TimeUnit.DAYS.toMillis(quantifiers[0]*29);
        if(time<0){
            event.getChannel().sendMessage("That is a long way away! Ask me to closer to the time").queue();
            return false;
        }
        String text;
        if(commandRaw.length()>inString.length()+1)
            text = commandRaw.substring(inString.length()+1);
        else
            text= " ";
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
                        logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                        database.removeReminder(guildid,channelid,userid,time,text);
                        return;
                    }
                    user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(user.getAsMention()+" You asked me to remind you of this: "+text)).queue();
                    database.removeReminder(guildid,channelid,userid,time,text);
                    return;
                }
            }
            if(user==null){
                logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                database.removeReminder(guildid,channelid,userid,time,text);
                return;
            }
            database.removeReminder(guildid,channelid,userid,time,text);
            channel.sendMessage(user.getAsMention()+" You asked me to remind you of this: ```"+text+"```").queue();
        };
        executor.schedule(remind,time-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        database.setReminder(guildid, channelid,userid,time,text);
        event.getChannel().sendMessage("Reminding you "+inString+": ```"+text+"```").queue();
        return true;
    }
    private boolean handleOn(MessageReceivedEvent event, String commandRaw){
        Pattern on = Pattern.compile(onDateRegex);
        Matcher onMatcher=on.matcher(commandRaw);
        if(onMatcher.find()){
            String onString = onMatcher.group();
            Date date = parseDate(onString);
            if(date==null){
                return false;
            }
            long time = date.getTime();
            if(time<0){
                event.getChannel().sendMessage("That is a long way away! Ask me to closer to the time").queue();
                return false;
            }
            String text;
            if(commandRaw.length()>(commandRaw.indexOf(onString)+onString.length()+1))
                text = commandRaw.substring(commandRaw.indexOf(onString)+onString.length()+1);
            else
                text= " ";
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
                            logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                            database.removeReminder(guildid,channelid,userid,time,text);
                            return;
                        }
                        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(user.getAsMention()+" You asked me to remind you of this: ```"+text+"```")).queue();
                        database.removeReminder(guildid,channelid,userid,time,text);
                        return;
                    }
                }
                if(user==null){
                    logger.printWarning("couldn't send reminder to user "+userid+" at guild "+guildid);
                    database.removeReminder(guildid,channelid,userid,time,text);
                    return;
                }
                database.removeReminder(guildid, channelid,userid,time,text);
                channel.sendMessage(user.getAsMention()+" You asked me to remind you of this: ```"+text+"```").queue();
            };
            executor.schedule(remind,time-System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            database.setReminder(guildid, channelid,userid,time,text);
            event.getChannel().sendMessage("Reminding you "+onString+": ```"+text+"```").queue();
            return true;
        }
        return false;
    }
}
