package succ.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Interface for bot interaction with commands, lays out the generic methods needed to run commands.
 */
public interface Command {
        String getDescription();
        int getAccessLevel();
        void run(MessageReceivedEvent event);
}
