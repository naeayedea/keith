package keith.commands;

import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public interface Command {

    String getShortDescription(String prefix);
    String getLongDescription();
    String getDefaultName();
    AccessLevel getAccessLevel();
    void run(MessageReceivedEvent event, List<String> tokens);

    default int getTimeOut(){
        return 10;
    }

}
