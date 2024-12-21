package com.naeayedea.keith.commands.lib.command.interactions;

import com.naeayedea.keith.commands.lib.command.Command;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithGracefulErrorException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface MessageContextCommand extends Command {

    /**
     * Runs the given command
     *
     * @param event  the event which triggered the command
     */
    void run(@NotNull MessageContextInteractionEvent event) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException;

}
