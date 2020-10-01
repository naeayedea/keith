package succ.commands;

import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.ServerManager;

/**
 * Interface for bot interaction with commands, lays out the generic methods needed to run commands.
 */
public interface Command {
        String getDescription(MessageReceivedEvent event);
        int getAccessLevel();
        void run(MessageReceivedEvent event);
        default int getTimeOut(){
                return 10;
        }

        default String getPrefix(MessageReceivedEvent event, ServerManager serverManager){
                if(event.getChannel() instanceof PrivateChannel){
                        return "?";
                }
                return serverManager.getServer(event.getGuild().getId()).getPrefix();
        }
}
