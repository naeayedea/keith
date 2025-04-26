package com.naeayedea.keith.platform.discord.lib.command;

import com.naeayedea.keith.core.exception.KeithExecutionException;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface StringSelectInteractionHandler {

    @NotNull List<String> getTriggerOptions();

    void handleStringSelectEvent(StringSelectInteractionEvent event) throws KeithExecutionException;
}
