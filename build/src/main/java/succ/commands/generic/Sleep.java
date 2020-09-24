package succ.commands.generic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Sleep extends UserCommand{
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void run(MessageReceivedEvent event) {
        while(true){

        }
    }
}
