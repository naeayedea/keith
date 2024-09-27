package com.naeayedea.config.commands;

import com.naeayedea.keith.commands.message.MessageCommand;
import com.naeayedea.keith.commands.message.admin.AbstractAdminCommand;
import com.naeayedea.keith.commands.message.admin.utilities.AbstractAdminUtilsCommand;
import com.naeayedea.keith.commands.message.admin.utilities.AbstractOwnerCommand;
import com.naeayedea.keith.commands.message.generic.AbstractUserCommand;
import com.naeayedea.keith.commands.message.info.AbstractInfoCommand;
import com.naeayedea.keith.commands.message.info.Help;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.MultiMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MessageCommandConfig {


    @Bean
    public Help baseHelp(List<AbstractUserCommand> userCommands, List<AbstractInfoCommand> infoCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        MultiMap<String, MessageCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, userCommands, List.of(defaultName));
        populateCommandMap(commandMap, infoCommands, List.of(defaultName));

        return new Help(commandMap, serverManager, defaultName, commandAliases);
    }

    @Bean
    public Help adminHelp(List<AbstractAdminCommand> adminCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        MultiMap<String, MessageCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, adminCommands, List.of(defaultName));

        return new Help(commandMap, serverManager, defaultName, commandAliases);
    }

    @Bean
    public Help adminUtilitiesHelp(List<AbstractOwnerCommand> ownerCommands, List<AbstractAdminUtilsCommand> adminUtilsCommands, ServerManager serverManager, @Value("${keith.commands.help.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.help.aliases}', ',')}") List<String> commandAliases) {
        MultiMap<String, MessageCommand> commandMap = new MultiMap<>();

        populateCommandMap(commandMap, ownerCommands, List.of(defaultName));
        populateCommandMap(commandMap, adminUtilsCommands, List.of(defaultName));

        return new Help(commandMap, serverManager, defaultName, commandAliases);
    }

    public static void populateCommandMap(MultiMap<String, MessageCommand> commandMap, List<? extends MessageCommand> commands, List<String> exclusions) {
        for (MessageCommand command : commands) {
            if (!exclusions.contains(command.getDefaultName())) {
                commandMap.putAll(command.getAliases(), command);
            }
        }
    }
}
