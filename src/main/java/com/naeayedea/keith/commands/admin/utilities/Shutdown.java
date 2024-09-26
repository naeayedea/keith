package com.naeayedea.keith.commands.admin.utilities;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Shutdown extends AbstractOwnerCommand {

    public Shutdown() {
        super("shutdown");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"shut down the bot completely\"";
    }

    @Override
    public String getLongDescription() {
        return "Will shutdown the bot and terminate all processes without restarting";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        event.getChannel().sendMessage("Goodbye").queue(success -> Utilities.runShutdownProcedure());
    }
}
