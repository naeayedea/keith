package keith.commands;

import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {

    String getShortDescription(String prefix);
    String getLongDescription(String prefix);

    AccessLevel getAccessLevel();
    void run(MessageReceivedEvent event);

    default int getTimeOut(){
        return 10;
    }

}
