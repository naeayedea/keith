package succ.commands.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.commands.generic.UserCommand;

public class Sleep extends AdminCommand {
    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "sleep: \"simulates a misbehaving command taking to long to test if bot kills it\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        while(true){

        }
    }
}
