package com.naeayedea.keith.platform.discord.listener.interaction;

import com.naeayedea.keith.common.i18n.TranslationProvider;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import com.naeayedea.keith.core.managers.KeithUserManager;
import com.naeayedea.keith.common.model.event.KeithEvent;
import com.naeayedea.keith.common.model.user.KeithUser;
import com.naeayedea.keith.common.util.MultiMap;
import com.naeayedea.keith.platform.discord.command.lib.interactions.MessageContextCommandHandler;
import com.naeayedea.keith.platform.discord.model.discordCommand.CommandInformation;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.naeayedea.keith.common.i18n.TranslationProvider.MESSAGE_COMMAND_DESCRIPTION_TRANSLATION_SUFFIX;

@Component
public class MessageContextInteractionListener extends AbstractSlashCommandEventListener<MessageContextInteractionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(MessageContextInteractionListener.class);

    private final KeithUserManager keithUserManager;

    private final Map<String, MessageContextCommandHandler> commands;

    private final Map<String, String> translationMappings;

    public MessageContextInteractionListener(@Qualifier("slash-command-data-list") List<CommandInformation> commandInformation, List<MessageContextCommandHandler> messageContextCommandHandlers, KeithUserManager keithUserManager, TranslationProvider translationProvider) {
        this.keithUserManager = keithUserManager;
        this.translationMappings = new HashMap<>();

        Map<String, MessageContextCommandHandler> commandHandlers = new HashMap<>();

        logger.info("Loaded {} message context command handlers", messageContextCommandHandlers.size());

        for (MessageContextCommandHandler command : messageContextCommandHandlers) {
            commandHandlers.put(command.getInternalName(), command);
        }

        MultiMap<String, MessageContextCommandHandler> commandMultiMap = new MultiMap<>();
        for (CommandInformation command : commandInformation) {
            if (command.getType().equals(Command.Type.MESSAGE)) {
                MessageContextCommandHandler handler = commandHandlers.get(command.getName());

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

    @EventListener
    @Async
    @Override
    public void onEvent(KeithEvent<MessageContextInteractionEvent> event) {
        super.onEvent(event);
    }

    @Override
    protected KeithUser getUser(MessageContextInteractionEvent event) {
        return keithUserManager.getUser(event.getUser().getId());
    }

    @Override
    protected boolean isBot(MessageContextInteractionEvent event) {
        User user = event.getUser();

        return user.isBot() || user.isSystem();
    }

    @Override
    public void onPermitted(MessageContextInteractionEvent event) throws Exception {
        logger.info("event name {}", event.getName());

        String baseEventName = translationMappings.get(event.getName());

        logger.info("event name translated to {}", baseEventName);

        MessageContextCommandHandler command = commands.get(baseEventName);

        if (command == null) {
            logger.error("No handler configured for event {}", event.getName());

            throw new KeithGracefulErrorException("This command has not been configured properly. Please contact the owner using /feedback");
        }

        if (!keithUserManager.getUser(event.getUser().getId()).hasPermission(command.getAccessLevel())) {
            throw new KeithPermissionException("You do not have permission to use this command");
        }

        command.run(event);
    }
}
