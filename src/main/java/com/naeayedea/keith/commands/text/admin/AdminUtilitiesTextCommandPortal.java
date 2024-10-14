package com.naeayedea.keith.commands.text.admin;

import com.naeayedea.keith.commands.text.AbstractTextCommandPortal;
import com.naeayedea.keith.commands.lib.command.AccessLevel;
import com.naeayedea.keith.commands.text.TextCommand;
import com.naeayedea.keith.commands.text.admin.utilities.AbstractAdminUtilsTextCommand;
import com.naeayedea.keith.commands.text.admin.utilities.AbstractOwnerTextCommand;
import com.naeayedea.keith.commands.text.info.HelpTextCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.util.MultiMap;
import com.naeayedea.keith.util.Utilities;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
public class AdminUtilitiesTextCommandPortal extends AbstractTextCommandPortal {

    private final Logger logger = LoggerFactory.getLogger(AdminUtilitiesTextCommandPortal.class);

    private MultiMap<String, TextCommand> commands;

    private final CandidateManager candidateManager;

    private final List<AbstractAdminUtilsTextCommand> adminUtilsCommandHandlers;

    private final List<AbstractOwnerTextCommand> ownerCommandHandlers;

    private final HelpTextCommand adminUtilitiesHelpTextCommand;

    public AdminUtilitiesTextCommandPortal(CandidateManager candidateManager, @Value("${keith.commands.admin.utilities.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.aliases}', ',')}") List<String> commandAliases, List<AbstractAdminUtilsTextCommand> adminUtilsCommandHandlers, List<AbstractOwnerTextCommand> ownerCommandHandlers, @Qualifier("adminUtilitiesHelp") HelpTextCommand adminUtilitiesHelpTextCommand) {
        super(defaultName, commandAliases);
        this.candidateManager = candidateManager;
        this.adminUtilsCommandHandlers = adminUtilsCommandHandlers;

        this.ownerCommandHandlers = ownerCommandHandlers;
        this.adminUtilitiesHelpTextCommand = adminUtilitiesHelpTextCommand;
    }


    @PostConstruct
    private void initialiseCommands() {
        logger.info("Initializing admin utilities commands.");

        commands = new MultiMap<>();

        Utilities.populateCommandMap(commands, ownerCommandHandlers, List.of(adminUtilitiesHelpTextCommand.getDefaultName()));
        Utilities.populateCommandMap(commands, adminUtilsCommandHandlers, List.of(adminUtilitiesHelpTextCommand.getDefaultName()));

        commands.putAll(adminUtilitiesHelpTextCommand.getAliases(), adminUtilitiesHelpTextCommand);

        logger.info("Loaded {} admin utilities aliases", commands.size());
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \":eyes:\"";
    }

    @Override
    public String getDescription() {
        return ":eyes: nunaya";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) throws KeithExecutionException, KeithPermissionException {
        TextCommand command = findCommand(tokens);
        if (command != null) {
            try {
                if (candidateManager.getCandidate(event.getAuthor().getId()).hasPermission(command.getAccessLevel())) {
                    command.run(event, tokens);
                } else {
                    throw new KeithPermissionException("You do not have permission to run this command.");
                }
            } catch (SQLException e) {
                throw new KeithExecutionException(e);
            }

        }
    }

    private TextCommand findCommand(List<String> list) {
        return commands.get(list.removeFirst().toLowerCase());
    }
}
