package com.naeayedea.keith.commands.admin;

import com.naeayedea.keith.commands.MessageCommand;
import com.naeayedea.keith.commands.info.Help;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.util.MultiMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class Admin extends AbstractAdminCommand {

    private MultiMap<String, MessageCommand> commands;
    private final ServerManager serverManager;
    private final CandidateManager candidateManager;

    public Admin(ServerManager serverManager, CandidateManager candidateManager) {
        super("admin", true,  true);

        this.serverManager = serverManager;
        this.candidateManager = candidateManager;

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
        this.commands = new MultiMap<>();

        commands.putAll(Arrays.asList("echo", "repeat"), new Echo());
        commands.putAll(Arrays.asList("setlevel", "updatelevel"), new SetCandidateLevel(candidateManager));
        commands.putAll(Arrays.asList("utils", "util", "utilities"), new UtilitiesAdmin(serverManager, candidateManager));
        commands.putAll(Arrays.asList("stats", "stat", "statistics"), new Stats(candidateManager));
        commands.putAll(Arrays.asList("setstatus", "newstatus"), new SetStatus());
        commands.putAll(Arrays.asList("help", "hlep", "dumb", "commands"), new Help(commands, serverManager));
        commands.put("sneaky", new Sneaky());
        commands.put("ban", new Ban(serverManager, candidateManager));
        commands.put("send", new SendMessage());
    }

    private MessageCommand findCommand(List<String> list) {
        String commandString = list.remove(0).toLowerCase();
        return commands.get(commandString);
    }

}
