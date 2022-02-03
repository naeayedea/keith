package keith.commands.admin.utilities;

import keith.commands.admin.AdminCommand;
import keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Restart extends AdminCommand {

    String defaultName;

    public Restart() {
        defaultName = "restart";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"restarts the bot\"";
    }

    @Override
    public String getLongDescription() {
        return "Will shutdown and relaunch the bots processes - useful after update";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        Utilities.restart(event);
    }
}
