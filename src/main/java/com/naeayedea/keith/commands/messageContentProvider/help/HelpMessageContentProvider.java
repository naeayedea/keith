package com.naeayedea.keith.commands.messageContentProvider.help;

import com.naeayedea.keith.commands.lib.MessageContext;
import com.naeayedea.keith.commands.lib.MessageEmbedProvider;
import com.naeayedea.keith.commands.lib.MessageStringSelectionMenuProvider;
import com.naeayedea.keith.commands.text.MessageCommand;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelpMessageContentProvider implements MessageEmbedProvider<HelpContextOptions>, MessageStringSelectionMenuProvider<HelpContextOptions> {

    private static final Logger logger = LoggerFactory.getLogger(HelpMessageContentProvider.class);

    private final String defaultEmbedTitle;

    private final Map<String, MessageCommand> commands;

    private final StringSelectMenu commandSelectionMenu;

    private final String commandSelectionMenuComponentId;

    public HelpMessageContentProvider(Map<String, MessageCommand> commandMap, String defaultEmbedTitle, String commandSelectionMenuComponentId) {
        this.defaultEmbedTitle = defaultEmbedTitle;
        this.commands = commandMap;
        this.commandSelectionMenuComponentId = commandSelectionMenuComponentId;

        this.commandSelectionMenu = createMessageSelects();
    }

    @Override
    public List<MessageEmbed> getEmbeds(MessageContext<HelpContextOptions> context) {
        logger.info("Got options {}, and arguments {}", context.getOptions(), context.getArguments());
        if (!context.getOptions().isEmpty() && context.getOptions().getFirst() == HelpContextOptions.COMMAND) {
            try {
                return List.of(getCommandHelpEmbed((MessageCommand) context.getArguments().get(0), context.getArguments().get(1).toString()));
            } catch (IndexOutOfBoundsException e) {
                logger.error("Unexpected number of arguments found in HelpMessageContentProvider. Got {}", context.getArguments());
                return List.of(getDefaultHelpEmbed());
            } catch (ClassCastException e) {
                logger.error("Unexpected type of arguments found in HelpMessageContentProvider. Got {}", context.getArguments());
                return List.of(getDefaultHelpEmbed());
            }
        } else {
            return List.of(getDefaultHelpEmbed());
        }
    }

    @Override
    public StringSelectMenu getStringSelectMenu(MessageContext<HelpContextOptions> context) {
        return commandSelectionMenu;
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

}
