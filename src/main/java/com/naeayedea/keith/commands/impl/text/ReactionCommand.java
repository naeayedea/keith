package com.naeayedea.keith.commands.impl.text;

import com.naeayedea.keith.commands.lib.command.Command;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;

/**
 * Defines an interface for running commands through the MessageReactionAddEvent listener
 */
public interface ReactionCommand extends Command {

    /**
     * Returns all reactions that will trigger the given command
     *
     * @return a list of Emoji representing reactions within Discord.
     */
    List<Emoji> getReactionTriggers();

    /**
     * Check if an Emoji should trigger the command
     *
     * @return true if the Emoji is present in the list of reaction triggers, false otherwise.
     */
    boolean triggeredBy(Emoji emoji);

    /**
     * Execute the command described by the implementing class
     *
     * @param event the discord event triggered by a message reaction
     */
    void run(MessageReactionAddEvent event, User user) throws KeithPermissionException, KeithExecutionException;

}
