package com.naeayedea.keith.platform.discord.listener;

import com.naeayedea.keith.core.model.event.KeithEvent;
import com.naeayedea.keith.core.model.user.KeithUser;
import com.naeayedea.keith.platform.discord.lib.command.interactions.SlashCommand;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlashCommandListener extends AbstractSlashCommandEventListener<SlashCommandInteractionEvent> {

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

    @EventListener
    @Async
    @Override
    public void onEvent(KeithEvent<SlashCommandInteractionEvent> eventSource) {
        super.onEvent(eventSource);
    }

    @Override
    public void onPermitted(SlashCommandInteractionEvent event) throws Exception {
        SlashCommand command = commands.get(event.getName());

        if (command != null) {
            if (!keithUserManager.getUser(event.getUser().getId()).hasPermission(command.getAccessLevel())) {
                throw new KeithPermissionException("You do not have permission to use this command");
            }

            command.run(event);
        } else {
            logger.error("No handler configured for event {}", event.getName());

            throw new KeithGracefulErrorException("This command has not been configured properly. Please contact the owner using /feedback");
        }
    }

    @Override
    protected KeithUser getUser(SlashCommandInteractionEvent event) {
        return keithUserManager.getUser(event.getUser().getId());
    }

    @Override
    protected boolean isBot(SlashCommandInteractionEvent event) {
        return event.getUser().isBot() || event.getUser().isSystem();
    }
}
