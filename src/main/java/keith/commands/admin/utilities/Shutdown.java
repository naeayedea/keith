package keith.commands.admin.utilities;

import keith.commands.admin.AdminCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Shutdown extends OwnerCommand {

    String defaultName;

    public Shutdown() {
        defaultName = "shutdown";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"shut down the bot completely\"";
    }

    @Override
    public String getLongDescription() {
        return "Will shutdown the bot and terminate all processes without restarting";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        event.getChannel().sendMessage("Goodbye").queue(success -> System.exit(0));
    }
}
