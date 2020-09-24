package succ.commands.generic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.ServerManager;

/**
 * Changes the prefix of the server the command is executed in
 * Stops the prefix being too long or nothing at all.
 */

public class SetPrefix extends UserCommand{

    private ServerManager serverManager;
    public SetPrefix(ServerManager serverManager){
        this.serverManager = serverManager;
    }

    @Override
    public String getDescription() {
        return "setprefix: \"Sets the current prefix of this server to a new value, size limit is 1\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        try{
        String newPrefix = event.getMessage().getContentDisplay().trim().split("\\s+")[1];
        if(newPrefix.length()>1){
            event.getChannel().sendMessage("Prefix has a max length of 1 and cannot contain spaces!").queue();
            return;
        }
        if(serverManager.setPrefix(event.getGuild().getId(), newPrefix)){
        event.getChannel().sendMessage("Prefix successfully updated to '"+newPrefix+"' tag the bot to see the prefix again").queue();
        return;
        }
        else {
            event.getChannel().sendMessage("Prefix change unsuccessful, please try another prefix").queue();
        }
        }
        catch(IndexOutOfBoundsException e){
            event.getChannel().sendMessage("Prefix cannot be empty!").queue();
        }
    }
}
