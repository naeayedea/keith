package com.naeayedea.keith.commands.info;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Invite extends InfoCommand {

    public Invite() {
        super("invite");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"Invite me to your other servers!\"";
    }

    @Override
    public String getLongDescription() {
        return "sends a link which can be used to invite keith to other servers!";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        Utilities.Messages.sendEmbed(event.getChannel(), "Invite Me!", "[https://keithbot.com/invite](https://discord.com/oauth2/authorize?client_id=624702573064224803&scope=bot&permissions=381648628854)", event);
    }
}
