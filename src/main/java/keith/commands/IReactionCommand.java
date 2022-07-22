package keith.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * Defines an interface for running commands through the MessageReactionAddEvent listener
 */
public interface IReactionCommand extends ICommand {

    /**
     * Execute the command described by the implementing class
     * @param event the discord event triggered by a message reaction
     */
    void run(MessageReactionAddEvent event, User user);

}
