package succ.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.commands.Command;
import succ.commands.admin.AdminCommand;
import succ.util.ServerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Prints a list of all user commands and their descriptions
 */
public class Help implements Command {

    private Map<String, Command> commands;
    private ServerManager serverManager;
    public Help(Map<String, Command> commands, ServerManager serverManager){
        this.commands = commands;
        this.serverManager = serverManager;
    }

    @Override
    public String getDescription() {
        return "help: \"this command, returns general command list\"";
    }

    @Override
    public int getAccessLevel() {
        return 1;
    }

    @Override
    public void run(MessageReceivedEvent event) {
        String prefix = serverManager.getServer(event.getGuild().getId()).getPrefix();
        String helpString = "```cs\n";
        for(Map.Entry<String, Command> set : commands.entrySet()){
            helpString+= prefix+set.getValue().getDescription()+"\n";
        }
        helpString+="```";
        event.getChannel().sendMessage(helpString).queue();

    }
}
