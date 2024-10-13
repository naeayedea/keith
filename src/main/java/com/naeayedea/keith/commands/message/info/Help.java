package com.naeayedea.keith.commands.message.info;

import com.naeayedea.keith.commands.message.MessageCommand;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;

public class Help extends AbstractInfoCommand {

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
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix + getDefaultName() + ": \"for more information on a command use " + prefix + "help [command]\"";
    }

    @Override
    public String getLongDescription() {
        return "Help lists all available commands as well as going into further detail when help on a specific command" +
            "is requested using one of its aliases";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        String prefix = event.isFromGuild() ? serverManager.getServer(event.getGuild().getId()).prefix() : DEFAULT_PREFIX;

        if (tokens.isEmpty()) {
            sendAllCommandHelp(event.getChannel(), prefix);
        } else {
            sendSingleCommandHelp(event.getChannel(), tokens.getFirst().toLowerCase());
        }
    }

    public void sendAllCommandHelp(MessageChannel channel, String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Help");
        eb.setColor(Utilities.getBotColor());
        eb.setDescription("For information on specific commands do \"" + prefix + "help [command]\"");
        StringBuilder helpString = new StringBuilder("```cs\n");
        for (MessageCommand command : commands.values()) {
            String description = command.getShortDescription(prefix);
            //only add description if not duplicate (need to look into improving multimap, maybe have two maps?)
            if (helpString.indexOf(description) == -1 && !command.isHidden()) {
                helpString.append(command.getShortDescription(prefix)).append("\n");
            }
        }
        helpString.append("```");
        eb.addField("List of Commands", helpString.toString(), false);
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void sendSingleCommandHelp(MessageChannel channel, String commandString) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(Utilities.getBotColor());

        MessageCommand command = commands.get(commandString);

        if (command != null) {

            eb.setTitle("Command: " + command.getDefaultName());

            StringBuilder knownAliases = new StringBuilder();

            commands.forEach((key, val) -> {
                //for every command, if command matches the requested command, add its aliases to the embed
                //do not add the default name.
                if (val.equals(command) && !key.equals(val.getDefaultName())) {
                    knownAliases.append(key).append(", ");
                }
            });

            eb.setDescription("Below is extended information on the command " + command.getDefaultName() + " and its known aliases");
            eb.addField("User Level", command.getAccessLevel().toString(), false);
            eb.addField("Is Hidden", command.isHidden() ? "True" : "False", false);
            eb.addField("Private Message Compatible", command.isPrivateMessageCompatible() ? "True" : "False", false);
            eb.addField("Time Out", command.getTimeOut() + "s", false);

            if (knownAliases.toString().trim().isEmpty()) {
                eb.addField("Known Aliases", "No aliases", false);
            } else {
                eb.addField("Known Aliases", knownAliases.substring(0, knownAliases.length() - 2), false);
            }

            eb.addField("Information", command.getLongDescription(), false);
            channel.sendMessageEmbeds(eb.build()).queue();
        } else {
            channel.sendMessage("No command found with that name or alias").queue();
        }


    }
}
