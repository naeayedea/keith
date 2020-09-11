package Commands.Admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Prints a list of admin commands and their description.
 */
public class Help implements AdminCommand{
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getAccessLevel() {
        return ADMIN;
    }

    @Override
    public void run(MessageReceivedEvent event) {

    }
}
