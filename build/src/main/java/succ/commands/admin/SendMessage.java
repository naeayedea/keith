package succ.commands.admin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import succ.util.MessageBuilder;
import succ.util.ServerManager;

import java.util.Arrays;

public class SendMessage extends AdminCommand{

    MessageBuilder builder;
    private ServerManager serverManager;

    public SendMessage(JDA jda, ServerManager serverManager){
        this.builder = new MessageBuilder(jda);
        this.serverManager = serverManager;
    }
    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "send: \"lets you send a message to another channel - do '"+super.getPrefix(event, serverManager)+"send message/embed [channelid] [title(embed only] [message]\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        String commandRaw = event.getMessage().getContentRaw().trim();
        String[] args = commandRaw.split("\\s+");
        System.out.println(Arrays.toString(args));
        try{
            String type = args[2];
            if(type.equals("message")){
                String channelId = args[3];
                String message = commandRaw.substring(commandRaw.indexOf(args[4]));
                builder.sendMessage(channelId, message, event);
            }
            else if(type.equals("embed")){
                String channelId = args[3];
                String title = args[4];
                String message = commandRaw.substring(commandRaw.indexOf(args[5]));
                builder.sendEmbed(channelId, title, message, event);
            }
            else if(type.equals("blast")){
                if(channel instanceof PrivateChannel){
                    channel.sendMessage("can't use blast in private message").queue();
                    return;
                }
                String message = commandRaw.substring(commandRaw.indexOf(args[3]));
                for(MessageChannel messageChannel : event.getGuild().getTextChannels()){
                    try{
                    messageChannel.sendMessage(message).queue();
                    }
                    catch(PermissionException e){
                        continue;
                    }
                }

            }
        } catch (IndexOutOfBoundsException e){
            channel.sendMessage("Insufficient arguments, see help").queue();
            return;
        }
    }
}
