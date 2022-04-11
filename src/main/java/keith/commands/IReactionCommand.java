package keith.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;

/**
 * Defines an interface for running commands through the MessageReactionAddEvent listener
 */
public interface IReactionCommand {

    /**
     * Execute the command described by the implementing class
     * @param event the discord event triggered by a message reaction
     */
    void run(MessageReactionAddEvent event, User user);

    /**
     * Determine if a command is usable in private messages
     * @return true if compatible, false otherwise.
     */
    boolean isPrivateMessageCompatible();
}
