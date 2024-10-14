package com.naeayedea.keith.commands.text.info;

import com.naeayedea.keith.commands.text.MessageCommand;
import com.naeayedea.keith.commands.text.StringSelectInteractionHandler;
import com.naeayedea.keith.exception.KeithException;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Help extends AbstractInfoCommand implements StringSelectInteractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(Help.class);

    private final Map<String, MessageCommand> commands;

    private final ServerManager serverManager;

    private final String defaultEmbedTitle;

    private final StringSelectMenu commandSelectionMenu;

    private final String commandSelectionMenuComponentId;

    @Value("${keith.defaultPrefix}")
    private String DEFAULT_PREFIX;

    public Help(Map<String, MessageCommand> commandMap, ServerManager serverManager, String defaultName, List<String> commandAliases, String defaultEmbedTitle) {
        super(defaultName, commandAliases);

        this.commands = commandMap;
        this.serverManager = serverManager;
        this.defaultEmbedTitle = defaultEmbedTitle;

        this.commandSelectionMenuComponentId = "command-selection-"+defaultEmbedTitle.toLowerCase().replace("\\S+", "-");

        this.commandSelectionMenu = createMessageSelects();
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

        if (tokens.isEmpty()) {
            sendAllCommandHelp(event.getChannel());
        } else {
            sendSingleCommandHelp(event.getChannel(), tokens.getFirst().toLowerCase(), prefix);
        }
    }

    public void sendAllCommandHelp(MessageChannel channel) {
        channel.sendMessageEmbeds(getDefaultHelpEmbed()).addActionRow(commandSelectionMenu).queue();
    }

    public void sendSingleCommandHelp(MessageChannel channel, String commandString, String prefix) {
        MessageCommand command = commands.get(commandString);

        if (command != null) {
            channel.sendMessageEmbeds(getCommandHelpEmbed(command, prefix)).addActionRow(commandSelectionMenu).queue();
        } else {
            channel.sendMessageEmbeds(getDefaultHelpEmbed()).addActionRow(commandSelectionMenu).queue();
        }
    }

    private MessageEmbed getDefaultHelpEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(this.defaultEmbedTitle);
        embedBuilder.setColor(Utilities.getBotColor());

        addHelpFooter(embedBuilder);

        return embedBuilder.build();
    }

    private void addHelpFooter(EmbedBuilder embedBuilder) {
        embedBuilder.addField("Need Command Information?", "Use the menu below to select information on a specific command or group of commands", false);
    }

    private <T extends MessageCommand> MessageEmbed getCommandHelpEmbed(T command, String prefix) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setColor(Utilities.getBotColor());
        embedBuilder.setTitle(command.getDefaultName().substring(0, 1).toUpperCase() + command.getDefaultName().substring(1).toLowerCase());
        embedBuilder.setDescription(command.getDescription());

        String knownAliases = commands.entrySet().stream().filter(entry -> entry.getValue().equals(command) && !entry.getKey().equals(entry.getValue().getDefaultName())).map(Map.Entry::getKey).collect(Collectors.joining(", "));

        embedBuilder.addField("Example Usage: ", command.getExampleUsage(prefix), false);
        embedBuilder.addField("User Level", command.getAccessLevel().toString(), false);
        embedBuilder.addField("Is Hidden", command.isHidden() ? "True" : "False", false);
        embedBuilder.addField("Private Message Compatible", command.isPrivateMessageCompatible() ? "True" : "False", false);
        embedBuilder.addField("Time Out", command.getTimeOut() + "s", false);

        if (knownAliases.trim().isEmpty()) {
            embedBuilder.addField("Known Aliases", "No aliases", false);
        } else {
            embedBuilder.addField("Known Aliases", knownAliases, false);
        }

        addHelpFooter(embedBuilder);

        return embedBuilder.build();
    }

    @Override
    public List<String> getTriggerOptions() {
        return List.of(commandSelectionMenuComponentId);
    }

    @Override
    public void handleStringSelectEvent(StringSelectInteractionEvent event) throws KeithException {
        if (event.getComponentId().equals(commandSelectionMenuComponentId)) {
            commands.get(event.getValues().getFirst());

            MessageCommand command = commands.get(event.getValues().getFirst());

            MessageEditAction action;
            if (command == null) {
                action = event.getMessage().editMessageEmbeds(getDefaultHelpEmbed());

            } else {
                action = event.getMessage().editMessageEmbeds(getCommandHelpEmbed(command, event.getGuild() != null ? serverManager.getServer(event.getGuild().getId()).prefix() : DEFAULT_PREFIX));
            }

            action.queue(message -> {
                event.editSelectMenu(commandSelectionMenu).queueAfter(1, TimeUnit.SECONDS);
            });
        }
    }

    private StringSelectMenu createMessageSelects() {
        StringSelectMenu.Builder messageMenu = StringSelectMenu.create(commandSelectionMenuComponentId);

        messageMenu.addOption("Learn How to Use This Menu", "default message help");

        Map<String, MessageCommand> distinctCommands = new HashMap<>();

        commands.values().forEach(command -> distinctCommands.put(command.getDefaultName(), command));

        distinctCommands.forEach((key, val) -> {
            if (messageMenu.getOptions().size() < 25) {
                messageMenu.addOption(val.getDefaultName().substring(0, 1).toUpperCase() + val.getDefaultName().substring(1).toLowerCase(), key.toLowerCase());
            } else {
                logger.warn("More than 25 command options at level: {}, consider grouping some commands.", defaultEmbedTitle);
            }
        });

        return messageMenu.build();
    }
}
