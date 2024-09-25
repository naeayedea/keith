package com.naeayedea.keith.commands.admin;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.Command;
import com.naeayedea.keith.commands.IMessageCommand;
import com.naeayedea.keith.commands.admin.utilities.Shutdown;
import com.naeayedea.keith.commands.admin.utilities.*;
import com.naeayedea.keith.commands.info.Help;
import com.naeayedea.keith.managers.UserManager;
import com.naeayedea.keith.util.MultiMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class AdminUtilities extends AdminCommand {

    MultiMap<String, IMessageCommand> commands;

    public AdminUtilities() {
        super("utils");
        initialiseCommands();
    }

    private void initialiseCommands() {
        commands = new MultiMap<>();
        commands.put("restart", new Restart());
        commands.put("uptime", new Uptime());
        commands.putAll(Arrays.asList("clear-cache", "clearcache", "clear"), new Clear());
        commands.putAll(Arrays.asList("database", "db", "sql"), new DatabaseSearch());
        commands.putAll(Arrays.asList("shutdown", "kill"), new Shutdown());
        commands.putAll(Arrays.asList("locate", "find"), new Locate());
        commands.putAll(Arrays.asList("help", "hlep", "dumb", "commands"), new Help(commands));
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
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        IMessageCommand command = findCommand(tokens);
        if (command != null) {
            if (UserManager.getInstance().getUser(event.getAuthor().getId()).hasPermission(command.getAccessLevel())) {
                command.run(event, tokens);
            } else {
                event.getChannel().sendMessage("You do not have access to this command").queue();
            }
        }
    }

    private IMessageCommand findCommand(List<String> list) {
        String commandString = list.remove(0).toLowerCase();
        return commands.get(commandString);
    }
}
