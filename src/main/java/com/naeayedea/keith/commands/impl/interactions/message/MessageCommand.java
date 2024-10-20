package com.naeayedea.keith.commands.impl.interactions.message;

import com.naeayedea.keith.commands.lib.command.Command;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public interface MessageCommand extends Command {

    /**
     * Runs the given command
     *
     * @param event  the event which triggered the command
     */
    void run(MessageContextInteractionEvent event) throws KeithPermissionException, KeithExecutionException;

}
