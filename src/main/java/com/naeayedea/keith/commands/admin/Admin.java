package com.naeayedea.keith.commands.admin;

import com.naeayedea.config.commands.CommandConfig;
import com.naeayedea.keith.commands.AbstractCommandPortal;
import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.MessageCommand;
import com.naeayedea.keith.commands.info.Help;
import com.naeayedea.keith.util.MultiMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Admin extends AbstractCommandPortal {

    private final Logger logger = LoggerFactory.getLogger(Admin.class);

    private MultiMap<String, MessageCommand> commands;

    private final List<AbstractAdminCommand> adminCommandHandlers;

    private final Help adminHelp;

    private final AdminUtilities adminUtilities;

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ADMIN;
    }

    public Admin(@Value("${keith.commands.admin.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.aliases}', ',')}") List<String> commandAliases, List<AbstractAdminCommand> adminCommandHandlers, @Qualifier("adminHelp") Help adminHelp, AdminUtilities adminUtilities) {
        super(defaultName, commandAliases, true,  true);

        this.adminCommandHandlers = adminCommandHandlers;
        this.adminHelp = adminHelp;
        this.adminUtilities = adminUtilities;

        initialiseCommands();
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"admin command portal, for authorised users only\"";
    }

    @Override
    public String getLongDescription() {
        return "Allows authorised users to access more powerful commands such as moderation, bot utilities and the database";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        //Do not need to scrutinise the user as much re access level etc. as EventHandler already did this.
        MessageCommand command = findCommand(tokens);

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

        CommandConfig.populateCommandMap(commands, adminCommandHandlers, List.of(adminHelp.getDefaultName()));

        commands.putAll(adminHelp.getAliases(), adminHelp);
        commands.putAll(adminUtilities.getAliases(), adminUtilities);

        logger.info("Loaded {} admin message command aliases", commands.size());
    }

    private MessageCommand findCommand(List<String> list) {
        if (list == null || list.isEmpty())
            return null;

        return commands.get(list.removeFirst().toLowerCase());
    }

}
