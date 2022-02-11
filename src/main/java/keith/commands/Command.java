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

    //Hidden commands won't be displayed to users but can still be accessed by people with sufficient level who know of them
    default boolean isHidden() {
        return false;
    }

    default int getTimeOut(){
        return 10;
    }

    default boolean isPrivateMessageCompatible() {
        return true;
    }

    default boolean sendTyping() {return true;}

}
