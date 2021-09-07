package keith.commands.admin;

import keith.commands.Command;
import keith.commands.info.Help;
import keith.managers.ServerManager;
import keith.managers.UserManager;
import keith.util.MultiMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class Admin extends AdminCommand {

    MultiMap<String, Command> commands;
    ServerManager serverManager;
    UserManager userManager;
    String defaultName;

    public Admin () {
        serverManager = ServerManager.getInstance();
        userManager = UserManager.getInstance();
        defaultName = "admin";
        initialiseCommands();
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"admin command portal, for authorised users only\"";
    }

    @Override
    public String getLongDescription() {
        return "Allows authorised users to access more powerful commands such as moderation, bot utilities and the database";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        //Do not need to scrutinise the user as much re access level etc. as EventHandler already did this.
        Command command = findCommand(tokens);
        if (command != null) {
            command.run(event, tokens);
        }
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    private void initialiseCommands() {
        commands = new MultiMap<>();
        commands.putAll(Arrays.asList("echo", "repeat"), new Echo());
        commands.putAll(Arrays.asList("setlevel", "updatelevel"), new SetUserLevel());
        commands.putAll(Arrays.asList("utils", "util", "utilities"), new AdminUtilities());
        commands.putAll(Arrays.asList("stats", "stat", "statistics"), new Stats());
        commands.putAll(Arrays.asList("setstatus", "newstatus"), new SetStatus());
        commands.putAll(Arrays.asList("help", "hlep", "dumb"), new Help(commands));
        commands.put("ban", new Ban());
        commands.put("send", new SendMessage());
    }

    private Command findCommand(List<String> list) {
        String commandString = list.remove(0).toLowerCase();
        return commands.get(commandString);
    }

}
