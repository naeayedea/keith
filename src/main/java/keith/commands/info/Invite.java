package keith.commands.info;

import keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Invite extends InfoCommand {

    private final String defaultName;

    public Invite() {
        this.defaultName = "invite";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"Invite me to your other servers!\"";
    }

    @Override
    public String getLongDescription() {
        return "sends a link which can be used to invite keith to other servers!";
    }

    @Override
    public String getDefaultName() {
        return this.defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        Utilities.Messages.sendEmbed(event.getChannel(), "Invite Me!", "[https://keithbot.com/invite](https://discord.com/oauth2/authorize?client_id=624702573064224803&scope=bot&permissions=381648628854)", event);
    }
}
