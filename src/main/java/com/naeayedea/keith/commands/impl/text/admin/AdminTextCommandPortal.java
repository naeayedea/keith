package com.naeayedea.keith.commands.impl.text.admin;

import com.naeayedea.keith.commands.impl.text.AbstractTextCommandPortal;
import com.naeayedea.keith.commands.lib.command.AccessLevel;
import com.naeayedea.keith.commands.impl.text.TextCommand;
import com.naeayedea.keith.commands.impl.text.info.HelpTextCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.util.MultiMap;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminTextCommandPortal extends AbstractTextCommandPortal {

    private final Logger logger = LoggerFactory.getLogger(AdminTextCommandPortal.class);

    private MultiMap<String, TextCommand> commands;

    private final List<AbstractAdminTextCommand> adminCommandHandlers;

    private final HelpTextCommand adminHelpTextCommand;

    private final AdminUtilitiesTextCommandPortal adminUtilitiesTextCommandPortal;

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ADMIN;
    }

    public AdminTextCommandPortal(@Value("${keith.commands.admin.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.aliases}', ',')}") List<String> commandAliases, List<AbstractAdminTextCommand> adminCommandHandlers, @Qualifier("adminHelp") HelpTextCommand adminHelpTextCommand, AdminUtilitiesTextCommandPortal adminUtilitiesTextCommandPortal) {
        super(defaultName, commandAliases, true, true);

        this.adminCommandHandlers = adminCommandHandlers;
        this.adminHelpTextCommand = adminHelpTextCommand;
        this.adminUtilitiesTextCommandPortal = adminUtilitiesTextCommandPortal;

        initialiseCommands();
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"admin command portal, for authorised users only\"";
    }

    @Override
    public String getDescription() {
        return "Allows authorised users to access more powerful commands such as moderation, bot utilities and the database";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) throws KeithPermissionException, KeithExecutionException {
        //Do not need to scrutinise the user as much re access level etc. as EventHandler already did this.
        TextCommand command = findCommand(tokens);

        if (command != null) {
            command.run(event, tokens);
        }
    }

    @Override
    public boolean sendTyping() {
        return false;
    }

    private void initialiseCommands() {
        logger.info("Initializing admin commands.");

        commands = new MultiMap<>();

        Utilities.populateCommandMap(commands, adminCommandHandlers, List.of(adminHelpTextCommand.getDefaultName()));

        commands.putAll(adminHelpTextCommand.getAliases(), adminHelpTextCommand);
        commands.putAll(adminUtilitiesTextCommandPortal.getAliases(), adminUtilitiesTextCommandPortal);

        logger.info("Loaded {} admin message command aliases", commands.size());
    }

    private TextCommand findCommand(List<String> list) {
        if (list == null || list.isEmpty())
            return null;

        return commands.get(list.removeFirst().toLowerCase());
    }

}
