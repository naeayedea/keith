package keith.commands.info;

import keith.commands.AccessLevel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help extends InfoCommand {


    @Override
    public String getShortDescription(String prefix) {
        return prefix+"help: for more information on a command use \""+prefix+"help [command]\"";
    }

    @Override
    public String getLongDescription(String prefix) {
        return null;
    }

    @Override
    public AccessLevel getAccessLevel() {
        return null;
    }

    @Override
    public void run(MessageReceivedEvent event) {

    }
}
