package keith.commands.generic;

import keith.managers.ServerManager;
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
        if (tokens.isEmpty()) {
            event.getChannel().sendMessage("Please enter a prefix, note that it cannot contain spaces!").queue();
        } else if (tokens.size() > 1 || containsInvalidCharacters(tokens.get(0))) {
            event.getChannel().sendMessage("Prefix can't contain spaces or non-ascii characters! Please only have one word").queue();
        } else {
            ServerManager.Server server = ServerManager.getInstance().getServer(event.getGuild().getId());
            if (server.setPrefix(tokens.get(0))) {
                event.getChannel().sendMessage("Prefix updated successfully to: '"+server.getPrefix()+"'").queue();
            }  else {
                event.getChannel().sendMessage("Could not set prefix, please contact bot owner").queue();
            }
        }
    }


    //This function checks for spaces that discord removes but isn't considered whitespace such as U+2800 braille space
    public boolean containsInvalidCharacters(String token) {
        boolean[] result = new boolean[1];
        token.chars().forEach(c -> result[0] = c >= 128);
        return result[0];
    }
}
