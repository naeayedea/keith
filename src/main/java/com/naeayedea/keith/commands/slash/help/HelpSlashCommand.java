package com.naeayedea.keith.commands.slash.help;

import com.naeayedea.keith.commands.common.AbstractCommand;
import com.naeayedea.keith.commands.lib.MessageContext;
import com.naeayedea.keith.commands.lib.command.AccessLevel;
import com.naeayedea.keith.commands.messageContentProvider.help.HelpContextOptions;
import com.naeayedea.keith.commands.messageContentProvider.help.HelpMessageContentProvider;
import com.naeayedea.keith.commands.slash.SlashCommand;
import com.naeayedea.keith.commands.text.TextCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HelpSlashCommand extends AbstractCommand implements SlashCommand {

    private final Map<String, TextCommand> commands;

    private final HelpMessageContentProvider messageContentProvider;

    public HelpSlashCommand(@Value("${keith.commands.help.defaultName}") String name, List<TextCommand> commandList) {
        super(name);

        this.commands = commandList
            .stream()
            .filter(command -> !command.getDefaultName().equalsIgnoreCase(name) && (command.getAccessLevel() == AccessLevel.USER || command.getAccessLevel() == AccessLevel.ALL))
            .collect(Collectors.toMap(TextCommand::getDefaultName, Function.identity()));

        this.messageContentProvider = new HelpMessageContentProvider(this.commands, name, "command-selection-" + name.toLowerCase().replace("\\S+", "-"));
    }

    @Override
    public void run(SlashCommandInteractionEvent event) throws KeithPermissionException, KeithExecutionException {
        String commandOption = event.getOption("command", "", OptionMapping::getAsString).toLowerCase();

        if (!commandOption.isEmpty() && commands.containsKey(commandOption)) {
            TextCommand command = commands.get(commandOption);

            event
                .reply("")
                .setEphemeral(true)
                .addEmbeds(messageContentProvider.getEmbeds(MessageContext.of(HelpContextOptions.COMMAND, List.of(command, "/"))))
                .addActionRow(messageContentProvider.getStringSelectMenu(MessageContext.empty()))
                .queue();
        } else {
            event
                .reply("")
                .setEphemeral(true)
                .addEmbeds(messageContentProvider.getEmbeds(MessageContext.of(HelpContextOptions.DEFAULT)))
                .addActionRow(messageContentProvider.getStringSelectMenu(MessageContext.empty()))
                .queue();
        }
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ALL;
    }
}
