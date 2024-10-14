package com.naeayedea.keith.commands.slash;

import com.naeayedea.keith.commands.lib.command.Command;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SlashCommand extends Command {

    /**
     * Runs the given command
     *
     * @param event  the event which triggered the command
     */
    void run(SlashCommandInteractionEvent event) throws KeithPermissionException, KeithExecutionException;

}
