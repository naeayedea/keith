package Commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Prints a list of all user commands and their descriptions
 */
public class Help implements Command{
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getAccessLevel() {
        return BANNED;
    }

    @Override
    public void run(MessageReceivedEvent event) {

    }
}
