package succ.commands.generic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.commands.Command;
import succ.commands.admin.AdminCommand;
import succ.util.ServerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Prints a list of all user commands and their descriptions
 */
public class Help extends UserCommand {

    private Map<String, Command> commands;
    private ServerManager serverManager;
    public Help(Map<String, Command> commands, ServerManager serverManager){
        this.commands = commands;
        this.serverManager = serverManager;
    }

    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "help: \"this command, returns general command list\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        String prefix = getPrefix(event, serverManager);
        String helpString = "```cs\n";
        for(Map.Entry<String, Command> set : commands.entrySet()){
            helpString+= prefix+set.getValue().getDescription(event)+"\n";
        }
        helpString+="```";
        event.getChannel().sendMessage(helpString).queue();

    }
}
