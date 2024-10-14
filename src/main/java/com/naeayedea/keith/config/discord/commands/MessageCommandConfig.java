package com.naeayedea.keith.config.discord.commands;

import com.naeayedea.keith.commands.text.TextCommand;
import com.naeayedea.keith.commands.text.admin.AbstractAdminTextCommand;
import com.naeayedea.keith.commands.text.admin.utilities.AbstractAdminUtilsTextCommand;
import com.naeayedea.keith.commands.text.admin.utilities.AbstractOwnerTextCommand;
import com.naeayedea.keith.commands.text.generic.AbstractUserTextCommand;
import com.naeayedea.keith.commands.text.info.AbstractInfoTextCommand;
import com.naeayedea.keith.commands.text.info.Help;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.MultiMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.naeayedea.keith.util.Utilities.populateCommandMap;

@Configuration
public class MessageCommandConfig {


    @Bean
    public Help baseHelp(List<AbstractUserTextCommand> userCommands, List<AbstractInfoTextCommand> infoCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        MultiMap<String, TextCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, userCommands, List.of(defaultName));
        populateCommandMap(commandMap, infoCommands, List.of(defaultName));

        return new Help(commandMap, serverManager, defaultName, commandAliases, "Help");
    }

    @Bean
    public Help adminHelp(List<AbstractAdminTextCommand> adminCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        MultiMap<String, TextCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, adminCommands, List.of(defaultName));

        return new Help(commandMap, serverManager, defaultName, commandAliases, "Admin Help");
    }

    @Bean
    public Help adminUtilitiesHelp(List<AbstractOwnerTextCommand> ownerCommands, List<AbstractAdminUtilsTextCommand> adminUtilsCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        MultiMap<String, TextCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, ownerCommands, List.of(defaultName));
        populateCommandMap(commandMap, adminUtilsCommands, List.of(defaultName));

        return new Help(commandMap, serverManager, defaultName, commandAliases, "Admin Utilities Help");
    }
}
