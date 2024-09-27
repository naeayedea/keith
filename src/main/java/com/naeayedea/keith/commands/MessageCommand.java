package com.naeayedea.keith.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public interface MessageCommand extends Command {

    /**
     * Returns all aliases that can be used to trigger the command
     * @return a list of aliases as String.
     */
    List<String> getAliases();

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

}
