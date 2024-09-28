package com.naeayedea.config.discord;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naeayedea.i18n.LocalizationRetriever;
import com.naeayedea.keith.model.discordCommand.*;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Configuration
public class DiscordCommandConfig {

    private static final String NAME_TRANSLATION_SUFFIX = "name";

    private static final String DESCRIPTION_TRANSLATION_SUFFIX = "desc.slash";

    private final MessageSource messageSource;


    public DiscordCommandConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Bean
    public List<CommandData> slashCommands(@Value("${keith.commands.config.location}") Resource location, ObjectMapper objectMapper, MessageSource messageSource) throws IOException {
        List<CommandData> commands = new ArrayList<>();

        try (InputStream input = location.getInputStream()) {

            List<CommandInformation> commandInformationList = objectMapper.readValue(input, new TypeReference<>() {
            });

            for (CommandInformation commandInformation : commandInformationList) {
                commands.add(processCommandInformation(commandInformation, messageSource));

            }
        }

        return commands;
    }

    private CommandData processCommandInformation(CommandInformation commandInformation, MessageSource messageSource) throws IOException {
        return unpackCommandInformation(commandInformation)
            .setGuildOnly(commandInformation.isGuildOnly())
            .setNSFW(commandInformation.isNSFW())
            .setDefaultPermissions(processDefaultPermission(commandInformation.getDefaultPermission()))
            .setLocalizationFunction(getLocalizationFunction(commandInformation));
    }

    private CommandData unpackCommandInformation(CommandInformation commandInformation) throws IOException {
        return switch (commandInformation.getType().toLowerCase()) {
            case "slash" -> processSlashCommand(commandInformation);
            case "message" ->
                Commands.message(getTranslation(getTranslationKey("", commandInformation.getName(), NAME_TRANSLATION_SUFFIX), Locale.getDefault()));
            case "user" ->
                Commands.user(getTranslation(getTranslationKey("", commandInformation.getName(), NAME_TRANSLATION_SUFFIX), Locale.getDefault()));
            default -> throw new IOException("Expected slash, message, or user. Got " + commandInformation.getType());
        };
    }

    private CommandData processSlashCommand(CommandInformation commandInformation) throws IOException {
        SlashCommandData slashCommand = Commands.slash(
            getTranslation(getTranslationKey("", commandInformation.getName(), NAME_TRANSLATION_SUFFIX), Locale.getDefault()),
            getTranslation(getTranslationKey("", commandInformation.getName(), DESCRIPTION_TRANSLATION_SUFFIX), Locale.getDefault())
        );

        if (!commandInformation.getSubCommandGroups().isEmpty() || !commandInformation.getSubCommands().isEmpty()) {
            if (!commandInformation.getOptions().isEmpty()) {
                throw new IOException("Options cannot be specified when subcommands are configured. Offending command: " + commandInformation.getName());
            }

            slashCommand.addSubcommands(buildSubCommandsFromSubCommandInformation(commandInformation.getSubCommands(), commandInformation.getName()));

            slashCommand.addSubcommandGroups(buildSubCommandGroupsFromCommandInformation(commandInformation.getSubCommandGroups(), commandInformation.getName()));
        } else {
            slashCommand.addOptions(buildOptionsFromCommandOptions(commandInformation.getOptions(), commandInformation.getName()));
        }

        return slashCommand;
    }

    private Collection<SubcommandGroupData> buildSubCommandGroupsFromCommandInformation(List<SubCommandGroup> subCommandGroups, String prefix) {
        return subCommandGroups.stream()
            .map(subCommandGroup -> {
                SubcommandGroupData subcommandGroupData = new SubcommandGroupData(
                    getTranslation(getTranslationKey(prefix, subCommandGroup.getName(), NAME_TRANSLATION_SUFFIX), Locale.getDefault()),
                    getTranslation(getTranslationKey(prefix, subCommandGroup.getName(), DESCRIPTION_TRANSLATION_SUFFIX), Locale.getDefault())
                );

                subcommandGroupData.addSubcommands(buildSubCommandsFromSubCommandInformation(subCommandGroup.getSubCommands(), prefix + "." + subCommandGroup.getName()));

                return subcommandGroupData;
            })
            .toList();
    }

    private Collection<SubcommandData> buildSubCommandsFromSubCommandInformation(List<SubCommandInformation> subCommandInformation, String prefix) {
        return subCommandInformation.stream()
            .map(subCommand -> {
                SubcommandData subcommandData = new SubcommandData(
                    getTranslation(getTranslationKey(prefix, subCommand.getName(), NAME_TRANSLATION_SUFFIX), Locale.getDefault()),
                    getTranslation(getTranslationKey(prefix, subCommand.getName(), DESCRIPTION_TRANSLATION_SUFFIX), Locale.getDefault())
                );

                subcommandData.addOptions(buildOptionsFromCommandOptions(subCommand.getOptions(), prefix + "." + subCommand.getName()));

                return subcommandData;
            })
            .toList();
    }

    private Collection<OptionData> buildOptionsFromCommandOptions(List<CommandOption> commandOptions, String prefix) {
        return commandOptions.stream()
            .map(option -> {
                OptionData optionData = new OptionData(
                    OptionType.valueOf(option.getType()),
                    getTranslation(getTranslationKey(prefix, option.getName(), NAME_TRANSLATION_SUFFIX), Locale.getDefault()),
                    getTranslation(getTranslationKey(prefix, option.getName(), DESCRIPTION_TRANSLATION_SUFFIX), Locale.getDefault())
                );

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

    private void populateLocalizationFunctionFromOptions(CommandLocalizationFunction localizationFunction, List<CommandOption> options, String prefix) {
        options.forEach(option ->
            populateNameAndDescription(
                localizationFunction,
                prefix + ".options." + option.getName(),
                discordLocale -> getTranslation(getTranslationKey(prefix, option.getName(), NAME_TRANSLATION_SUFFIX), discordLocale.toLocale()),
                discordLocale -> getTranslation(getTranslationKey(prefix, option.getName(), DESCRIPTION_TRANSLATION_SUFFIX), discordLocale.toLocale())
            )
        );
    }

    private void populateLocalizationFunctionFromSubCommands(CommandLocalizationFunction localizationFunction, SubCommandInformation subCommandInformation, String prefix) {
        populateNameAndDescription(
            localizationFunction,
            prefix + "." + subCommandInformation.getName(),
            discordLocale -> getTranslation(getTranslationKey(prefix, subCommandInformation.getName(), NAME_TRANSLATION_SUFFIX), discordLocale.toLocale()),
            discordLocale -> getTranslation(getTranslationKey(prefix, subCommandInformation.getName(), DESCRIPTION_TRANSLATION_SUFFIX), discordLocale.toLocale())
        );

        populateLocalizationFunctionFromOptions(localizationFunction, subCommandInformation.getOptions(), prefix + "." + subCommandInformation.getName());
    }

    private void populateLocalizationFunctionFroSubCommandGroups(CommandLocalizationFunction localizationFunction, List<SubCommandGroup> subCommandGroups, String prefix) {
        subCommandGroups.forEach(group -> {
                populateNameAndDescription(
                    localizationFunction,
                    prefix + "." + group.getName(),
                    discordLocale -> getTranslation(getTranslationKey(prefix, group.getName(), NAME_TRANSLATION_SUFFIX), discordLocale.toLocale()),
                    discordLocale -> getTranslation(getTranslationKey(prefix, group.getName(), DESCRIPTION_TRANSLATION_SUFFIX), discordLocale.toLocale())
                );

                group.getSubCommands().forEach(subCommand -> populateLocalizationFunctionFromSubCommands(localizationFunction, subCommand, prefix + "." + group.getName()));
            }
        );
    }

    private void populateNameAndDescription(CommandLocalizationFunction localizationFunction, String prefix, LocalizationRetriever nameRetriever, LocalizationRetriever descriptionRetriever) {
        Map<DiscordLocale, String> nameLocalization = new HashMap<>();
        Map<DiscordLocale, String> descriptionLocalization = new HashMap<>();

        for (DiscordLocale discordLocale : DiscordLocale.values()) {
            //discord doesn't accept the unknown locale, this is a JDA construct.
            if (discordLocale.equals(DiscordLocale.UNKNOWN))
                continue;

            nameLocalization.put(discordLocale, nameRetriever.getLocalization(discordLocale));
            descriptionLocalization.put(discordLocale, descriptionRetriever.getLocalization(discordLocale));
        }

        localizationFunction.registerLocalization(prefix + ".name", nameLocalization);
        localizationFunction.registerLocalization(prefix + ".description", descriptionLocalization);
    }

    private CommandLocalizationFunction getLocalizationFunction(CommandInformation commandInformation) {
        CommandLocalizationFunction localizationFunction = new CommandLocalizationFunction();

        String commandPrefix = commandInformation.getName();

        populateNameAndDescription(
            localizationFunction,
            commandPrefix,
            discordLocale -> getTranslation(getTranslationKey("", commandInformation.getName(), NAME_TRANSLATION_SUFFIX), discordLocale.toLocale()),
            discordLocale -> getTranslation(getTranslationKey("", commandInformation.getName(), DESCRIPTION_TRANSLATION_SUFFIX), discordLocale.toLocale())
        );

        populateLocalizationFunctionFroSubCommandGroups(localizationFunction, commandInformation.getSubCommandGroups(), commandPrefix);

        commandInformation.getSubCommands().forEach(subCommand -> populateLocalizationFunctionFromSubCommands(localizationFunction, subCommand, commandInformation.getName()));

        populateLocalizationFunctionFromOptions(localizationFunction, commandInformation.getOptions(), commandPrefix);

        return localizationFunction;
    }

    private String getTranslation(String key, Locale locale) {
        return getTranslation(key, new Object[]{}, locale);
    }

    private String getTranslation(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }

    private String getTranslationKey(String prefix, String name, String target) {
        return "translation.i18n." + (prefix.isEmpty() ? "" : prefix + ".") + name + "." + target;
    }
}
