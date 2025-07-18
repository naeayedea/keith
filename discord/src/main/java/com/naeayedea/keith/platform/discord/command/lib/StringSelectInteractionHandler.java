package com.naeayedea.keith.platform.discord.command.lib;

import com.naeayedea.keith.common.exception.KeithExecutionException;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface StringSelectInteractionHandler {

    @NotNull List<String> getTriggerOptions();

    void handleStringSelectEvent(StringSelectInteractionEvent event) throws KeithExecutionException;
}
