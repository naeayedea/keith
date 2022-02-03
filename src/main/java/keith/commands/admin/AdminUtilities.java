package keith.commands.admin;

import keith.commands.AccessLevel;
import keith.commands.Command;
import keith.commands.admin.utilities.*;
import keith.commands.info.Help;
import keith.managers.UserManager;
import keith.util.MultiMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class AdminUtilities extends AdminCommand {

    String defaultName;
    MultiMap<String, Command> commands;

    public AdminUtilities() {
        defaultName = "utils";
        initialiseCommands();
    }

    private void initialiseCommands() {
        commands = new MultiMap<>();
        commands.put("restart", new Restart());
        commands.put("uptime", new Uptime());
        commands.putAll(Arrays.asList("clear-cache", "clearcache", "clear"), new Clear());
        commands.putAll(Arrays.asList("database", "db"), new DatabaseSearch());
        commands.putAll(Arrays.asList("shutdown", "kill"), new Shutdown());
        commands.putAll(Arrays.asList("locate", "find"), new Locate());
        commands.putAll(Arrays.asList("help", "hlep", "dumb", "commands"), new Help(commands));
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \":eyes:\"";
    }

    @Override
    public String getLongDescription() {
        return ":eyes: nunaya";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        Command command = findCommand(tokens);
        if (command != null) {
            if (UserManager.getInstance().getUser(event.getAuthor().getId()).hasPermission(command.getAccessLevel())) {
                command.run(event, tokens);
            } else {
                event.getChannel().sendMessage("You do not have access to this command").queue();
            }
        }
    }

    private Command findCommand(List<String> list) {
        String commandString = list.remove(0).toLowerCase();
        return commands.get(commandString);
    }
}
