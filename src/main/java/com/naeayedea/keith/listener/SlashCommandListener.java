package com.naeayedea.keith.listener;

import com.naeayedea.keith.commands.impl.interactions.slash.SlashCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.model.discordCommand.CommandInformation;
import com.naeayedea.keith.util.MultiMap;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlashCommandListener {

    private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);

    private final CandidateManager candidateManager;

    private final Map<String, SlashCommand> commands;

    public SlashCommandListener(@Qualifier("slash-command-data-list") List<CommandInformation> commandInformation, List<SlashCommand> slashCommands, CandidateManager candidateManager) {
        this.candidateManager = candidateManager;
        Map<String, SlashCommand> commandHandlers = new HashMap<>();

        logger.info("Loaded {} slash commands handlers", slashCommands.size());

        for (SlashCommand command : slashCommands) {
            commandHandlers.put(command.getDefaultName(), command);
        }

        MultiMap<String, SlashCommand> commandMultiMap = new MultiMap<>();

        for (CommandInformation command : commandInformation) {
            SlashCommand handler = commandHandlers.get(command.getName());

            if (handler != null) {
                commandMultiMap.put(command.getName(), handler);
            } else {
                logger.warn("No slash command handler found for command {}", command.getName());
            }
        }

        logger.info("Loaded {} slash commands entries", commandMultiMap.size());

        this.commands = commandMultiMap;
    }

    @EventListener(SlashCommandInteractionEvent.class)
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        SlashCommand command = commands.get(event.getName());

        if (command != null) {
            try {
                try {
                    if (!candidateManager.getCandidate(event.getUser().getId()).hasPermission(command.getAccessLevel())) {
                        throw new KeithPermissionException("You do not have permission to use this command");
                    }

                    command.run(event);
                } catch (KeithExecutionException e) {
                    logger.error("Error encountered whilst running command {}, {}", event.getName(), e.getMessage(), e);
                    throw new IOException(e);
                } catch (KeithPermissionException e) {
                    event.reply("You do not have permission to do that!")
                        .setEphemeral(true)
                        .queue();
                }
            } catch (Throwable e) {
                event.reply("Something went wrong :(")
                    .setEphemeral(true)
                    .queue();
            }

        } else {
            logger.error("No handler configured for event {}", event.getName());

            event.reply("This command has not been configured properly. Please contact the owner using /feedback")
                .setEphemeral(true)
                .queue();
        }
    }
}
