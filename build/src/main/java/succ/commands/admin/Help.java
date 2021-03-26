package succ.commands.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.commands.Command;
import succ.util.ServerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Prints a list of admin commands and their description.
 */
public class Help extends AdminCommand{

    private Map<String, Command> commands;
    private ServerManager serverManager;
    public Help(Map<String, Command> commands, ServerManager serverManager){
        this.commands=commands;
        this.serverManager=serverManager;
    }

    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "help: \"this command, returns admin command list\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        String prefix = getPrefix(event, serverManager);
        String helpString = "```cs\n";
        for(Map.Entry<String, Command> set : commands.entrySet()){
            helpString+= prefix+"admin "+set.getValue().getDescription(event)+"\n";
        }
        helpString+="```";
        event.getChannel().sendMessage(helpString).queue();

    }
}
