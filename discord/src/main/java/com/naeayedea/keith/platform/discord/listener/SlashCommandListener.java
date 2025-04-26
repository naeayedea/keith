package com.naeayedea.keith.platform.discord.listener;

import com.naeayedea.keith.platform.discord.lib.command.interactions.SlashCommand;
import com.naeayedea.keith.core.exception.KeithExecutionException;
import com.naeayedea.keith.core.exception.KeithGracefulErrorException;
import com.naeayedea.keith.core.exception.KeithPermissionException;
import com.naeayedea.keith.core.managers.KeithUserManager;
import com.naeayedea.keith.platform.discord.model.discordCommand.CommandInformation;
import com.naeayedea.keith.core.util.MultiMap;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
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

    private final KeithUserManager keithUserManager;

    private final Map<String, SlashCommand> commands;

    public SlashCommandListener(@Qualifier("slash-command-data-list") List<CommandInformation> commandInformation, List<SlashCommand> slashCommands, KeithUserManager keithUserManager) {
        this.keithUserManager = keithUserManager;
        Map<String, SlashCommand> commandHandlers = new HashMap<>();

        logger.info("Loaded {} slash commands handlers", slashCommands.size());

        for (SlashCommand command : slashCommands) {
            commandHandlers.put(command.getDefaultName(), command);
        }

        MultiMap<String, SlashCommand> commandMultiMap = new MultiMap<>();

        for (CommandInformation command : commandInformation) {
            if (command.getType().equals(Command.Type.SLASH)) {
                SlashCommand handler = commandHandlers.get(command.getName());

                if (handler != null) {
                    commandMultiMap.put(command.getName(), handler);
                } else {
                    logger.warn("No slash command handler found for command {}", command.getName());
                }
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
                    if (!keithUserManager.getUser(event.getUser().getId()).hasPermission(command.getAccessLevel())) {
                        throw new KeithPermissionException("You do not have permission to use this command");
                    }

                    command.run(event);
                } catch (KeithGracefulErrorException e) {
                    event
                        .reply(e.getMessage())
                        .setEphemeral(true)
                        .queue();
                } catch (KeithPermissionException e) {
                    event.reply("You do not have permission to do that!")
                        .setEphemeral(true)
                        .queue();
                } catch (KeithExecutionException e) {
                    logger.error("Error encountered whilst running command {}, {}", event.getName(), e.getMessage(), e);
                    throw new IOException(e);
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
