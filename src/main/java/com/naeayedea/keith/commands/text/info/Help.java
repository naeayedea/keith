package com.naeayedea.keith.commands.text.info;

import com.naeayedea.keith.commands.lib.MessageContext;
import com.naeayedea.keith.commands.messageContentProvider.help.HelpContextOptions;
import com.naeayedea.keith.commands.messageContentProvider.help.HelpMessageContentProvider;
import com.naeayedea.keith.commands.text.MessageCommand;
import com.naeayedea.keith.commands.text.StringSelectInteractionHandler;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.managers.ServerManager;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Help extends AbstractInfoCommand implements StringSelectInteractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(Help.class);

    private final Map<String, MessageCommand> commands;

    private final ServerManager serverManager;

    private final String commandSelectionMenuComponentId;

    private final HelpMessageContentProvider messageContentProvider;

    @Value("${keith.defaultPrefix}")
    private String DEFAULT_PREFIX;

    public Help(Map<String, MessageCommand> commandMap, ServerManager serverManager, String defaultName, List<String> commandAliases, String defaultEmbedTitle) {
        super(defaultName, commandAliases);

        this.commands = commandMap;
        this.serverManager = serverManager;

        this.commandSelectionMenuComponentId = "command-selection-"+defaultEmbedTitle.toLowerCase().replace("\\S+", "-");

        messageContentProvider = new HelpMessageContentProvider(commandMap, defaultEmbedTitle,commandSelectionMenuComponentId);
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"for more information on a command use " + prefix + "help [command]\"";
    }

    @Override
    public String getDescription() {
        return "Help lists all available commands as well as going into further detail when help on a specific command" +
            "is requested using one of its aliases";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) throws KeithExecutionException, KeithPermissionException {
        String prefix = event.isFromGuild() ? serverManager.getServer(event.getGuild().getId()).prefix() : DEFAULT_PREFIX;

        if (!tokens.isEmpty() && commands.get(tokens.getFirst().toLowerCase()) != null) {
            MessageCommand command = commands.get(tokens.getFirst().toLowerCase());

            event
                .getChannel()
                .sendMessageEmbeds(messageContentProvider.getEmbeds(MessageContext.of(HelpContextOptions.COMMAND, List.of(command, prefix))))
                .addActionRow(messageContentProvider.getStringSelectMenu(MessageContext.empty()))
                .queue();
        } else {
            event
                .getChannel()
                .sendMessageEmbeds(messageContentProvider.getEmbeds(MessageContext.of(HelpContextOptions.DEFAULT)))
                .addActionRow(messageContentProvider.getStringSelectMenu(MessageContext.empty()))
                .queue();
        }
    }

    @Override
    public List<String> getTriggerOptions() {
        return List.of(commandSelectionMenuComponentId);
    }

    @Override
    public void handleStringSelectEvent(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals(commandSelectionMenuComponentId)) {
            commands.get(event.getValues().getFirst());

            MessageCommand command = commands.get(event.getValues().getFirst());

            MessageEditAction action;
            if (command == null) {
                action = event.getMessage().editMessageEmbeds(messageContentProvider.getEmbeds(MessageContext.of(HelpContextOptions.DEFAULT)));

            } else {
                action = event.getMessage().editMessageEmbeds(messageContentProvider.getEmbeds(MessageContext.of(HelpContextOptions.COMMAND, List.of(command, event.getGuild() != null ? serverManager.getServer(event.getGuild().getId()).prefix() : DEFAULT_PREFIX))));
            }

            action.queue(message -> event.editSelectMenu(messageContentProvider.getStringSelectMenu(MessageContext.empty())).queueAfter(1, TimeUnit.SECONDS));
        }
    }

}
