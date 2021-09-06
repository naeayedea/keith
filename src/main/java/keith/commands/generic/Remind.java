package keith.commands.generic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Remind extends UserCommand {

    String defaultName;

    public Remind() {
        defaultName = "remind";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"set a reminder and the bot will message you after the specified timeframe!\"";
    }

    @Override
    public String getLongDescription() {
        return "TODO" ;
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {

    }
}
