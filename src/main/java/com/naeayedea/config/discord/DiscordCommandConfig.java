package com.naeayedea.config.discord;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naeayedea.keith.model.discordCommand.*;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Configuration
public class DiscordCommandConfig {

    @Bean
    public List<CommandData> slashCommands(@Value("${keith.commands.config.location}") Resource location, ObjectMapper objectMapper) throws IOException {
        List<CommandData> commands = new ArrayList<>();

        try (InputStream input = location.getInputStream()) {

            List<CommandInformation> commandInformationList = objectMapper.readValue(input, new TypeReference<>() {});

            for (CommandInformation commandInformation : commandInformationList) {
                commands.add(processCommandInformation(commandInformation));
            }
        }

        return commands;
    }

    private CommandData processCommandInformation(CommandInformation commandInformation) throws IOException {
        return unpackCommandInformation(commandInformation)
            .setGuildOnly(commandInformation.isGuildOnly())
            .setNSFW(commandInformation.isNSFW())
            .setDefaultPermissions(processDefaultPermission(commandInformation.getDefaultPermission()))
            .setLocalizationFunction(getLocalizationFunction());

    }

    private CommandData unpackCommandInformation(CommandInformation commandInformation) throws IOException {
        return switch (commandInformation.getType().toLowerCase()) {
            case "slash" -> processSlashCommand(commandInformation);
            case "message" -> Commands.message(commandInformation.getName());
            case "user" -> Commands.user(commandInformation.getName());
            default -> throw new IOException("Expected slash, message, or user. Got "+ commandInformation.getType());
        };
    }

    private CommandData processSlashCommand(CommandInformation commandInformation) throws IOException {
        SlashCommandData slashCommand = Commands.slash(commandInformation.getName(), commandInformation.getDescription());

        if (!commandInformation.getSubCommandGroups().isEmpty() || !commandInformation.getSubCommands().isEmpty()) {
            if (!commandInformation.getOptions().isEmpty()) {
                throw new IOException("Options cannot be specified when subcommands are configured. Offending command: "+ commandInformation.getName());
            }

            slashCommand.addSubcommands(buildSubCommandsFromSubCommandInformation(commandInformation.getSubCommands()));

            slashCommand.addSubcommandGroups(buildSubCommandGroupsFromCommandInformation(commandInformation.getSubCommandGroups()));
        } else {
            slashCommand.addOptions(buildOptionsFromCommandOptions(commandInformation.getOptions()));
        }

        return slashCommand;
    }

    private Collection<SubcommandGroupData> buildSubCommandGroupsFromCommandInformation(List<SubCommandGroup> subCommandGroups) {
        return subCommandGroups.stream()
            .map(subCommandGroup -> {
                SubcommandGroupData subcommandGroupData = new SubcommandGroupData(subCommandGroup.getName(), subCommandGroup.getDescription());

                subcommandGroupData.addSubcommands(buildSubCommandsFromSubCommandInformation(subCommandGroup.getSubCommands()));

                return subcommandGroupData;
            })
            .toList();
    }

    private Collection<SubcommandData> buildSubCommandsFromSubCommandInformation(List<SubCommandInformation> subCommandInformation) {
        return subCommandInformation.stream()
            .map(subCommand -> {
                SubcommandData subcommandData = new SubcommandData(subCommand.getName(), subCommand.getDescription());

                subcommandData.addOptions(
                    subCommand.getOptions().stream()
                        .map(option -> {
                            OptionData optionData = new OptionData(OptionType.valueOf(option.getType()), option.getName(), option.getDescription());

                            for (CommandChoice choice : option.getChoices()) {
                                optionData.addChoice(choice.getName(), choice.getValue());
                            }

                            return optionData;
                        })
                        .toList()
                );

                return subcommandData;
            })
            .toList();
    }

    private Collection<OptionData> buildOptionsFromCommandOptions(List<CommandOption> commandOptions) {
        return commandOptions.stream()
            .map(option -> {
                OptionData optionData = new OptionData(OptionType.valueOf(option.getType()), option.getName(), option.getDescription());

                for (CommandChoice choice : option.getChoices()) {
                    optionData.addChoice(choice.getName(), choice.getValue());
                }

                return optionData;
            })
            .toList();
    }

    private DefaultMemberPermissions processDefaultPermission(String defaultPermission) throws IOException {
        return switch (defaultPermission) {
            case "ENABLED" -> DefaultMemberPermissions.ENABLED;
            case "DISABLED" -> DefaultMemberPermissions.DISABLED;
            default -> throw new IOException("Unexpected default permission, currently support ENABLED and DISABLED");
        };
    }


    private LocalizationFunction getLocalizationFunction() {
        return key -> {return new HashMap<>(); };
    }

}
