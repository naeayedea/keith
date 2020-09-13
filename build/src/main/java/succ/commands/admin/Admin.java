package succ.commands.admin;

import succ.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

/**
 * Allows any user with administrator permissions to perform additional commands (warning - can be dangerous)
 */
public class Admin extends AdminCommand {

    private Map<String, Command> admin_commands;
    @Override
    public String getDescription() {
        return "gateway for all admin commands - do [prefix]admin [command]";
    }

    @Override
    public void run(MessageReceivedEvent event) {

    }
}
