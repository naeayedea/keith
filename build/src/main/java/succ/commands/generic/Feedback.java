package succ.commands.generic;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.UserManager;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Feedback extends UserCommand{

    UserManager userManager;
    JDA jda;
    public Feedback(UserManager userManagera){
        this.userManager = userManager;
    }
    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "feedback: \"bugs or ideas for the bot? use this command to voice your opinion\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        try{
            jda = event.getJDA();
            MessageChannel feedbackChannel= jda.getTextChannelById("760226764529336350");
            MessageChannel channel = event.getChannel();
            Message message = event.getMessage();
            String commandRaw = message.getContentRaw().trim();
            String[] commandSplit = commandRaw.split("\\s+");
            if(userManager.getUser(message.getAuthor().getId()).getAccessLevel() > 2 && commandSplit[1].equals("respond")){ //e.g. if user is an admin responding to a previous feedback
                String response = commandRaw.substring(commandRaw.indexOf(commandSplit[3]));
                feedbackChannel.retrieveMessageById(commandSplit[2]).queue((feedback) -> {
                    Pattern pattern = Pattern.compile("C:.*\\((\\d+?)\\)");
                    Matcher matcher = pattern.matcher(feedback.getContentDisplay());
                    if(matcher.find()){
                        MessageChannel respondChannel =  jda.getTextChannelById(matcher.group(1));
                        try{
                        respondChannel.sendMessage("Response from "+message.getAuthor()+": ```"+response+"```").queue();
                        }
                        catch(Exception e){
                            Pattern userPattern = Pattern.compile("U:.*\\((\\d+?)\\)\\s");
                            Matcher userMatcher = userPattern.matcher(feedback.getContentDisplay());
                            String userId = "ass";
                            if(userMatcher.find()){
                                userId=userMatcher.group(1);
                            }
                            User user = jda.getUserById(userId);
                            user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("Response from "+message.getAuthor()+": ```"+response+"```")).queue();
                            feedbackChannel.sendMessage("responded to feedback privately").queue();
                            return;
                        }
                        channel.sendMessage("Responded to feedback at channel "+respondChannel).queue();
                    }
                    else{
                        feedbackChannel.sendMessage("Error finding channel id").queue();
                    }

                }, (failure) -> {
                    event.getChannel().sendMessage("Something went wrong, please try again (in getresponse)").queue();
                });
                return;
        }
            if(channel instanceof PrivateChannel){
                feedbackChannel.sendMessage("Feedback from "+message.getAuthor()+" privately, "+message.getChannel()+": ```"+commandRaw.substring(commandRaw.indexOf(commandSplit[1]))+"```").queue();
                event.getChannel().sendMessage("Feedback sent!").queue();
                return;
            }
            feedbackChannel.sendMessage("Feedback from "+message.getAuthor()+" in server "+message.getGuild()+", channel "+message.getChannel()+": ```"+commandRaw.substring(commandRaw.indexOf(commandSplit[1]))+"```").queue();
            event.getChannel().sendMessage("Feedback sent!").queue();
        }
        catch (Exception e){
            e.printStackTrace();
            event.getChannel().sendMessage("Something went wrong, please try again").queue();
        }
    }
}
