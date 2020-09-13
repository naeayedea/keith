package succ.commands.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Prints a list of admin commands and their description.
 */
public class Help extends AdminCommand{

    private Map<String, AdminCommand> commands;
    public Help(Map<String, AdminCommand> commands){
        this.commands=commands;
    }

    @Override
    public String getDescription() {
        return "help: \"this command, returns admin command list\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        String helpString = "```cs\n";
        for(Map.Entry<String, AdminCommand> set : commands.entrySet()){
            helpString+= set.getValue().getDescription()+"\n";
        }
        helpString+="```";
        event.getChannel().sendMessage(helpString).queue();

    }
}
