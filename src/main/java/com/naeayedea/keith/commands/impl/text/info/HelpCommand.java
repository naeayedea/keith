package com.naeayedea.keith.commands.impl.text.info;

import com.naeayedea.keith.commands.impl.interactions.slash.SlashCommand;
import com.naeayedea.keith.commands.impl.text.generic.AbstractUserCommand;
import com.naeayedea.keith.commands.lib.MessageContext;
import com.naeayedea.keith.commands.impl.common.messageContentProvider.help.HelpContextOptions;
import com.naeayedea.keith.commands.impl.text.TextCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.MultiMap;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.naeayedea.keith.util.Utilities.populateCommandMap;

@Component
public class HelpCommand extends BaseHelpCommand implements SlashCommand {

    public HelpCommand(List<AbstractUserCommand> userCommands, List<AbstractInfoCommand> infoCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        super(buildCommandMap(userCommands, infoCommands, defaultName), serverManager, defaultName, commandAliases, "Help");
    }

    private static Map<String, TextCommand> buildCommandMap(List<AbstractUserCommand> userCommands, List<AbstractInfoCommand> infoCommands, String defaultName) {
        MultiMap<String, TextCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, userCommands, List.of(defaultName));
        populateCommandMap(commandMap, infoCommands, List.of(defaultName));

        return commandMap;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) throws KeithPermissionException, KeithExecutionException {
        String commandOption = event.getOption("command", "", OptionMapping::getAsString).toLowerCase();

        if (!commandOption.isEmpty() && getCommandMap().containsKey(commandOption)) {
            TextCommand command = getCommandMap().get(commandOption);

            event
                .reply("")
                .setEphemeral(true)
                .addEmbeds(getMessageContentProvider().getEmbeds(MessageContext.of(HelpContextOptions.COMMAND, List.of(command, "/"))))
                .addActionRow(getMessageContentProvider().getStringSelectMenu(MessageContext.empty()))
                .queue();
        } else {
            event
                .reply("")
                .setEphemeral(true)
                .addEmbeds(getMessageContentProvider().getEmbeds(MessageContext.of(HelpContextOptions.DEFAULT)))
                .addActionRow(getMessageContentProvider().getStringSelectMenu(MessageContext.empty()))
                .queue();
        }
    }

}
