package com.naeayedea.keith.commands.lib.command;

import com.naeayedea.keith.exception.KeithExecutionException;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.List;

public interface StringSelectInteractionHandler {

    List<String> getTriggerOptions();

    void handleStringSelectEvent(StringSelectInteractionEvent event) throws KeithExecutionException;
}
