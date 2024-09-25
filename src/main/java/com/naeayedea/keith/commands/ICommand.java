package com.naeayedea.keith.commands;

/**
 * Defines universal properties of commands regardless of source
 */
public interface ICommand {

    /**
     * Determine if a command is usable in private messages
     * @return true if compatible, false otherwise.
     */
    boolean isPrivateMessageCompatible();

    /**
     * Retrieve the {@link AccessLevel AccessLevel} of the command
     * @return an {@link AccessLevel AccessLevel} object representing the permissions required for a command
     * @see AccessLevel
     */
    AccessLevel getAccessLevel();

    /**
     * Get the message timeout of a command
     * @return the timeout period of a command in seconds
     */
    int getTimeOut();

    /**
     * Determine if this command should send a typing message to discord when the command is run
     * @return true if the command should send a typing signal, false otherwise
     */
    boolean sendTyping();

    /**
     * Returns the cost of running this command with respect to rate limiting
     */
    int getCost();
}
