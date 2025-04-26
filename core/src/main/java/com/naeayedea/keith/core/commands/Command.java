package com.naeayedea.keith.core.commands;

import org.springframework.lang.NonNull;

/**
 * Defines universal properties of commands regardless of source
 */
public interface Command {

    /**
     * Retrieve the {@link AccessLevel AccessLevel} of the command
     *
     * @return an {@link AccessLevel AccessLevel} object representing the permissions required for a command
     * @see AccessLevel
     */
    @NonNull
    AccessLevel getAccessLevel();

    /**
     * Get the message timeout of a command
     *
     * @return the timeout period of a command in seconds
     */
    int getTimeOut();

    /**
     * Determine if this command should send a typing message to discord when the command is run
     *
     * @return true if the command should send a typing signal, false otherwise
     */
    boolean sendTyping();

    /**
     * If the command can be used in a private channel
     */
    boolean isPrivateMessageCompatible();

    /**
     * Returns the cost of running this command with respect to rate limiting
     */
    int getCost();

    /**
     * Return the default name of the command
     *
     * @return the default name of the command
     */
    @NonNull String getDefaultName();
}
