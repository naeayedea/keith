package succ.util;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Utility class for creating and sending messages to text channels the bot can see.
 */
public class MessageBuilder {


    private JDA jda;
    public MessageBuilder(JDA jda){
        this.jda=jda;
    }
    public void sendEmbed(String channelID, String title, String message, MessageReceivedEvent event){
        try{
            TextChannel channel = jda.getTextChannelById(channelID);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(title);
            eb.setDescription(message);
            channel.sendMessage(eb.build()).queue();
        }
        catch (IllegalArgumentException e){
            event.getChannel().sendMessage("Message send error").queue();
            return;
        }
        catch (NullPointerException e){
            event.getChannel().sendMessage("Channel Unavailable, check id or for voice").queue();
            return;
        }
    }

    public void sendMessage(String channelID, String message, MessageReceivedEvent event ){
        try
        {
            TextChannel channel = jda.getTextChannelById(channelID);
            channel.sendMessage(message).queue();
            event.getChannel().sendMessage("Message sent successfully to "+channel.toString()).queue();
        }
        catch (IllegalArgumentException e){
            event.getChannel().sendMessage("Message send error").queue();
            return;
        }
        catch (NullPointerException e){
            event.getChannel().sendMessage("Channel Unavailable, check id or for voice").queue();
            return;
        }
    }


    //gotta learn how to do this
    public void sendImage(){

    }
}
