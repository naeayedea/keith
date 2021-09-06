package keith.commands.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Echo extends AdminCommand {

    String defaultName;

    public Echo() {
        defaultName = "echo";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"bot simply returns the text included in the command minus the prefix/command\"";
    }

    @Override
    public String getLongDescription() {
        return "Echo command is self explanatory, the bot will echo any text given to it minus the prefix+command";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        StringBuilder response = new StringBuilder();
        tokens.forEach(string -> response.append(string).append(" "));
        event.getChannel().sendMessage(response).queue();
    }
}
