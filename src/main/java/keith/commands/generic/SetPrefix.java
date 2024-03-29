package keith.commands.generic;

import keith.managers.ServerManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SetPrefix extends UserCommand {

    int limit;
    public SetPrefix() {
        super("setprefix");
        limit = 10;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"sets the prefix of the bot in your server, for prefix limits do "+prefix+"help setprefix!\"";
    }

    @Override
    public String getLongDescription() {
        return "Default prefix clashing with other bots? use setprefix to set a new one! Prefix must be ascii characters excluding spaces and must be less than "+limit+" characters";
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        if (tokens.isEmpty()) {
            event.getChannel().sendMessage("Please enter a prefix, note that it can't contain spaces or non-ascii characters or be longer than "+limit+" characters!").queue();
            return;
        }
        String newPrefix = tokens.get(0).trim().toLowerCase();
        if (tokens.size() > 1 || containsInvalidCharacters(newPrefix) || newPrefix.length() > limit) {
            event.getChannel().sendMessage("Prefix can't contain spaces or non-ascii characters or be longer than "+limit+" characters!").queue();
        } else {
            ServerManager.Server server = ServerManager.getInstance().getServer(event.getGuild().getId());
            if (server.setPrefix(newPrefix)) {
                event.getChannel().sendMessage("Prefix updated successfully to: '"+server.getPrefix()+"'").queue();
            }  else {
                event.getChannel().sendMessage("Could not set prefix, please contact bot owner").queue();
            }
        }
    }


    //This function checks ensures that the characters within the new prefix are within the desired range of ascii characters
    public boolean containsInvalidCharacters(String token) {
        return token.chars().mapToObj(c -> (char) c).map(c -> c > 32 && c < 127).anyMatch(b -> !b);
    }
}
