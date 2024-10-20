package com.naeayedea.keith.commands.impl.text.admin.utilities;

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
public class AdminUtilitiesHelpCommand extends BaseHelpCommand {

    public AdminUtilitiesHelpCommand(List<AbstractOwnerTextCommand> ownerCommands, List<AbstractAdminUtilsTextCommand> adminUtilsCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        super(buildCommandMap(ownerCommands, defaultName), serverManager, defaultName, commandAliases, "Admin Utilities Help");
    }

    private static Map<String, TextCommand> buildCommandMap(List<AbstractOwnerTextCommand> ownerTextCommands,  String defaultName) {
        MultiMap<String, TextCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, ownerTextCommands, List.of(defaultName));

        return commandMap;
    }
}
