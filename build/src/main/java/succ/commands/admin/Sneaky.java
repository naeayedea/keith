package succ.commands.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Sneaky is a command which attempts to give any bot admin a role
 * with server admin if the bot has sufficient permissions.
 */
public class Sneaky extends AdminCommand{

    @Override
    public void run(MessageReceivedEvent event){
        event.getChannel().sendMessage("titty").queue();
    }

    @Override
    public String getDescription() {
        return "sneaky: \"attempts to give any bot admin a role with server admin if the bot has permissions\"";
    }

}
