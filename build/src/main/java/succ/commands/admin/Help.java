package succ.commands.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.ServerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Prints a list of admin commands and their description.
 */
public class Help extends AdminCommand{

    private Map<String, AdminCommand> commands;
    private ServerManager serverManager;
    public Help(Map<String, AdminCommand> commands, ServerManager serverManager){
        this.commands=commands;
        this.serverManager=serverManager;
    }

    @Override
    public String getDescription() {
        return "help: \"this command, returns admin command list\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        String prefix = serverManager.getServer(event.getGuild().getId()).getPrefix();
        String helpString = "```cs\n";
        for(Map.Entry<String, AdminCommand> set : commands.entrySet()){
            helpString+= prefix+set.getValue().getDescription()+"\n";
        }
        helpString+="```";
        event.getChannel().sendMessage(helpString).queue();

    }
}
