package com.naeayedea.keith.commands.admin;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SetStatus extends AdminCommand {

    public SetStatus() {
        super("setstatus");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"sets the bots status to the specified message\"";
    }

    @Override
    public String getLongDescription() {
        return "sets the bot status to the specified message, can also do \"" +
                "setstatus default\" to return the bot status to the default setting";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        if (tokens.size() == 1 && tokens.get(0).equalsIgnoreCase("default")){
            Utilities.forceDefaultStatus();
        } else {
            Utilities.setStatus(Utilities.stringListToString(tokens));
        }
    }
}
