package com.naeayedea.keith.commands.admin;

import com.naeayedea.config.commands.CommandConfig;
import com.naeayedea.keith.commands.AbstractCommandPortal;
import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.MessageCommand;
import com.naeayedea.keith.commands.admin.utilities.AbstractAdminUtilsCommand;
import com.naeayedea.keith.commands.admin.utilities.AbstractOwnerCommand;
import com.naeayedea.keith.commands.info.Help;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.util.MultiMap;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminUtilities extends AbstractCommandPortal {

    private final Logger logger = LoggerFactory.getLogger(AdminUtilities.class);

    private MultiMap<String, MessageCommand> commands;

    private final CandidateManager candidateManager;

    private final List<AbstractAdminUtilsCommand> adminUtilsCommandHandlers;

    private final List<AbstractOwnerCommand> ownerCommandHandlers;

    private final Help adminUtilitiesHelp;

    public AdminUtilities(CandidateManager candidateManager, @Value("${keith.commands.admin.utilities.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.aliases}', ',')}") List<String> commandAliases, List<AbstractAdminUtilsCommand> adminUtilsCommandHandlers, List<AbstractOwnerCommand> ownerCommandHandlers, @Qualifier("adminUtilitiesHelp") Help adminUtilitiesHelp) {
        super(defaultName, commandAliases);
        this.candidateManager = candidateManager;
        this.adminUtilsCommandHandlers = adminUtilsCommandHandlers;

        this.ownerCommandHandlers = ownerCommandHandlers;
        this.adminUtilitiesHelp = adminUtilitiesHelp;
    }


    @PostConstruct
    private void initialiseCommands() {
        logger.info("Initializing admin utilities commands.");

        commands = new MultiMap<>();

        CommandConfig.populateCommandMap(commands, ownerCommandHandlers, List.of(adminUtilitiesHelp.getDefaultName()));
        CommandConfig.populateCommandMap(commands, adminUtilsCommandHandlers, List.of(adminUtilitiesHelp.getDefaultName()));

        commands.putAll(adminUtilitiesHelp.getAliases(), adminUtilitiesHelp);

        logger.info("Loaded {} admin utilities aliases", commands.size());
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \":eyes:\"";
    }

    @Override
    public String getLongDescription() {
        return ":eyes: nunaya";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageCommand command = findCommand(tokens);
        if (command != null) {
            if (candidateManager.getCandidate(event.getAuthor().getId()).hasPermission(command.getAccessLevel())) {
                command.run(event, tokens);
            } else {
                event.getChannel().sendMessage("You do not have access to this command").queue();
            }
        }
    }

    private MessageCommand findCommand(List<String> list) {
        return commands.get(list.removeFirst().toLowerCase());
    }
}
