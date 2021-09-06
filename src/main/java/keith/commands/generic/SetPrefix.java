package keith.commands.generic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SetPrefix extends UserCommand {

    String defaultName;

    public SetPrefix() {
        defaultName = "setprefix";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"sets the prefix of the bot in your server, new prefix cannot contain spaces!\"";
    }

    @Override
    public String getLongDescription() {
        return "TODO";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {

    }
}
