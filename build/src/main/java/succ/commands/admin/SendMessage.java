package succ.commands.admin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.MessageBuilder;

import java.util.Arrays;

public class SendMessage extends AdminCommand{

    MessageBuilder builder;
    public SendMessage(JDA jda){
        this.builder = new MessageBuilder(jda);
    }
    @Override
    public String getDescription() {
        return "send: \"Lets you send a message to another channel - do '[prefix]send message/embed [channelid] [title(embed only] [message]\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        System.out.println("here0");
        MessageChannel channel = event.getChannel();
        String commandRaw = event.getMessage().getContentRaw().trim();
        String[] args = commandRaw.split("\\s+");
        System.out.println(Arrays.toString(args));
        try{
            String type = args[2];
            if(type.equals("message")){
                System.out.println("here1");
                String channelId = args[3];
                String message = commandRaw.substring(commandRaw.indexOf(args[4]));
                builder.sendMessage(channelId, message, event);
            }
            else if(type.equals("embed")){
                System.out.println("here2");
                String channelId = args[3];
                String title = args[4];
                String message = commandRaw.substring(commandRaw.indexOf(args[5]));
                builder.sendEmbed(channelId, title, message, event);
            }
        } catch (IndexOutOfBoundsException e){
            channel.sendMessage("Insufficient arguments, see help").queue();
            System.out.println("fuck");
            return;
        }
    }
}
