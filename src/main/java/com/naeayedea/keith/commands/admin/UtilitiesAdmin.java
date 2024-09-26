package com.naeayedea.keith.commands.admin;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.commands.MessageCommand;
import com.naeayedea.keith.commands.admin.utilities.Shutdown;
import com.naeayedea.keith.commands.admin.utilities.*;
import com.naeayedea.keith.commands.info.Help;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.MultiMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class UtilitiesAdmin extends AbstractAdminCommand {

    private MultiMap<String, MessageCommand> commands;

    private final ServerManager serverManager;

    private final CandidateManager candidateManager;


    public UtilitiesAdmin(ServerManager serverManager, CandidateManager candidateManager) {
        super("utils");
        this.serverManager = serverManager;
        this.candidateManager = candidateManager;
        initialiseCommands();
    }

    private void initialiseCommands() {
        commands = new MultiMap<>();
        commands.put("restart", new Restart());
        commands.put("uptime", new Uptime());
        commands.putAll(Arrays.asList("clear-cache", "clearcache", "clear"), new Clear(serverManager, candidateManager));
        commands.putAll(Arrays.asList("database", "db", "sql"), new DatabaseSearch());
        commands.putAll(Arrays.asList("shutdown", "kill"), new Shutdown());
        commands.putAll(Arrays.asList("locate", "find"), new Locate());
        commands.putAll(Arrays.asList("help", "hlep", "dumb", "commands"), new Help(commands, serverManager));
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
