package com.naeayedea.keith.platform.discord.command.lib.interactions;

import com.naeayedea.keith.core.exception.KeithExecutionException;
import com.naeayedea.keith.core.exception.KeithGracefulErrorException;
import com.naeayedea.keith.core.exception.KeithPermissionException;
import com.naeayedea.keith.platform.discord.command.lib.CommandHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.lang.NonNull;

public interface SlashCommandHandler extends CommandHandler {

    /**
     * Runs the given command
     *
     * @param event the event which triggered the command
     */
    void run(@NonNull SlashCommandInteractionEvent event) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException;

}
