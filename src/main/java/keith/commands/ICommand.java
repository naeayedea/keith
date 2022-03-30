package keith.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public interface ICommand {

    /**
     * Returns a short description of the command, used when help is used to show all commands in a category
     * @param prefix the prefix of the server/channel
     * @return a String containing the description
     */
    String getShortDescription(String prefix);

    /**
     * Return a longer description of the command which should show use cases etc. - used when help [command] is used to
     * retrieve specific help for a command
     * @return a String containing the long description
     */
    String getLongDescription();

    /**
     * Return the default name of the command
     * @return the default name of the command
     */
    String getDefaultName();

    /**
     * Retrieve the {@link AccessLevel AccessLevel} of the command
     * @return an {@link AccessLevel AccessLevel} object representing the permissions required for a command
     * @see AccessLevel
     */
    AccessLevel getAccessLevel();

    /**
     * Runs the given command
     * @param event the event which triggered the command
     * @param tokens a list of tokens of the message from the user
     */
    void run(MessageReceivedEvent event, List<String> tokens);

    /**
     * Determine if a command is hidden
     * @return true if hidden, false otherwise
     */
    boolean isHidden();

    /**
     * Get the message timeout of a command
     * @return the timeout period of a command in seconds
     */
    int getTimeOut();

    /**
     * Determine if a command is usable in private messages
     * @return true if compatible, false otherwise.
     */
    boolean isPrivateMessageCompatible();

    /**
     * Determine if this command should send a typing message to discord when the command is run
     * @return true if the command should send a typing signal, false otherwise
     */
    boolean sendTyping();

}
