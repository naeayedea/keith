package com.naeayedea.keith.platform.discord.command.lib.interactions;

import com.naeayedea.keith.core.commands.lib.Command;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.springframework.lang.NonNull;

public interface MessageContextCommandHandler extends Command {

    /**
     * Runs the given command
     *
     * @param event  the event which triggered the command
     */
    void run(@NonNull MessageContextInteractionEvent event) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException;

}
