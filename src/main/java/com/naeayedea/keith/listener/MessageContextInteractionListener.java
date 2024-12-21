package com.naeayedea.keith.listener;

import com.naeayedea.keith.commands.lib.command.interactions.MessageContextCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithGracefulErrorException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.i18n.TranslationProvider;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.model.discordCommand.CommandInformation;
import com.naeayedea.keith.util.MultiMap;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
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

import static com.naeayedea.keith.i18n.TranslationProvider.MESSAGE_COMMAND_DESCRIPTION_TRANSLATION_SUFFIX;

@Component
public class MessageContextInteractionListener {


    private static final Logger logger = LoggerFactory.getLogger(MessageContextInteractionListener.class);

    private final CandidateManager candidateManager;


    private final Map<String, MessageContextCommand> commands;

    private final Map<String, String> translationMappings;

    public MessageContextInteractionListener(@Qualifier("slash-command-data-list") List<CommandInformation> commandInformation, List<MessageContextCommand> messageContextCommands, CandidateManager candidateManager, TranslationProvider translationProvider) {
        this.candidateManager = candidateManager;
        this. translationMappings = new HashMap<>();

        Map<String, MessageContextCommand> commandHandlers = new HashMap<>();

        logger.info("Loaded {} message context command handlers", messageContextCommands.size());

        for (MessageContextCommand command : messageContextCommands) {
            commandHandlers.put(command.getDefaultName(), command);
        }

        MultiMap<String, MessageContextCommand> commandMultiMap = new MultiMap<>();
        for (CommandInformation command : commandInformation) {
            if (command.getType().equals(Command.Type.MESSAGE)) {
                MessageContextCommand handler = commandHandlers.get(command.getName());

                if (handler != null) {
                    commandMultiMap.put(command.getName(), handler);

                    //now we map all the commands translations to its base name
                    for (DiscordLocale discordLocale : DiscordLocale.values()) {
                        //discord doesn't accept the unknown locale, this is a JDA construct.
                        if (discordLocale.equals(DiscordLocale.UNKNOWN))
                            continue;

                        translationMappings.put(translationProvider.getTranslation(translationProvider.getTranslationKey("", command.getName(), MESSAGE_COMMAND_DESCRIPTION_TRANSLATION_SUFFIX), discordLocale.toLocale()), command.getName());
                    }
                } else {
                    logger.warn("No message context command handler found for command {}", command.getName());
                }
            }
        }

        logger.info("Loaded {} message context commands entries", commandMultiMap.size());
        logger.info("loaded translations: {}", translationMappings);

        this.commands = commandMultiMap;
    }

    @EventListener(MessageContextInteractionEvent.class)
    public void onMessageContextInteractionEvent(MessageContextInteractionEvent event) {
        logger.info("event name {}", event.getName());

        String baseEventName = translationMappings.get(event.getName());

        logger.info("event name translated to {}", baseEventName);

        MessageContextCommand command = commands.get(baseEventName);

        if (command != null) {
            try {
                try {
                    if (!candidateManager.getCandidate(event.getUser().getId()).hasPermission(command.getAccessLevel())) {
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

                logger.warn("Encountered problem running command {}", command.getDefaultName(), e);
            }

        } else {
            logger.error("No handler configured for event {}", event.getName());

            event.reply("This command has not been configured properly. Please contact the owner using /feedback")
                .setEphemeral(true)
                .queue();
        }
    }
}
