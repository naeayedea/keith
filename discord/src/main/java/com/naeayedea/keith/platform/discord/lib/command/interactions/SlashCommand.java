package com.naeayedea.keith.platform.discord.lib.command.interactions;

import com.naeayedea.keith.core.commands.Command;
import com.naeayedea.keith.core.exception.KeithExecutionException;
import com.naeayedea.keith.core.exception.KeithGracefulErrorException;
import com.naeayedea.keith.core.exception.KeithPermissionException;
import org.springframework.lang.NonNull;

public interface SlashCommand extends Command {

    /**
     * Runs the given command
     *
     * @param event  the event which triggered the command
     */
    void run(@NonNull Object event) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException;

}
