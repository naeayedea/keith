package com.naeayedea.keith.platform.discord.lib.command.interactions;

import com.naeayedea.keith.core.commands.Command;
import com.naeayedea.keith.core.exception.KeithExecutionException;
import com.naeayedea.keith.core.exception.KeithGracefulErrorException;
import com.naeayedea.keith.core.exception.KeithPermissionException;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.springframework.lang.NonNull;

public interface MessageContextCommand extends Command {

    /**
     * Runs the given command
     *
     * @param event  the event which triggered the command
     */
    void run(@NonNull MessageContextInteractionEvent event) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException;

}
