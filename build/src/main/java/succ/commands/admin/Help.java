package succ.commands.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Prints a list of admin commands and their description.
 */
public class Help extends AdminCommand{
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void run(MessageReceivedEvent event) {

    }
}
