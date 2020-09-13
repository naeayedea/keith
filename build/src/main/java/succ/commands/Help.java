package succ.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.commands.Command;
import succ.commands.admin.AdminCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Prints a list of all user commands and their descriptions
 */
public class Help implements Command {

    private Map<String, Command> commands;
    public Help(Map<String, Command> commands){
        this.commands = commands;
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
        String helpString = "```cs\n";
        for(Map.Entry<String, Command> set : commands.entrySet()){
            helpString+= set.getValue().getDescription()+"\n";
        }
        helpString+="```";
        event.getChannel().sendMessage(helpString).queue();

    }
}
