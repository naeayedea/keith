package com.naeayedea.keith.commands.impl.text.admin;

import com.naeayedea.keith.commands.impl.text.TextCommand;
import com.naeayedea.keith.commands.impl.text.info.BaseHelpCommand;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.MultiMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.naeayedea.keith.util.Utilities.populateCommandMap;

@Component
public class AdminHelpCommand extends BaseHelpCommand {
    public AdminHelpCommand(List<AbstractAdminTextCommand> adminCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        super(buildCommandMap(adminCommands, defaultName), serverManager, defaultName, commandAliases, "Admin Help");
    }

    private static Map<String, TextCommand> buildCommandMap(List<AbstractAdminTextCommand> adminCommands, String defaultName) {
        MultiMap<String, TextCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, adminCommands, List.of(defaultName));

        return commandMap;
    }
}
