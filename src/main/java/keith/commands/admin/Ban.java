package keith.commands.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Ban extends AdminCommand {

    String defaultName;

    public Ban() {
        defaultName = "ban";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"\"";
    }

    @Override
    public String getLongDescription() {
        return null;
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {

    }
}
