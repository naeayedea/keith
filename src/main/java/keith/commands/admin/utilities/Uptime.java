package keith.commands.admin.utilities;

import keith.commands.admin.AdminCommand;
import keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Uptime extends AdminCommand {

    public Uptime() {
        super("uptime");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"get uptime\"";
    }

    @Override
    public String getLongDescription() {
        return "Returns the current uptime since last restart or major disconnect";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
       event.getChannel().sendMessage(Utilities.getUptimeString()).queue();
    }
}
