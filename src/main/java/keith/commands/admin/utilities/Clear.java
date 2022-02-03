package keith.commands.admin.utilities;

import keith.commands.admin.AdminCommand;
import keith.managers.ServerManager;
import keith.managers.UserManager;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Clear extends AdminCommand {

    String defaultName;

    public Clear() {
        defaultName = "clear-cache";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"clear manager caches\"";
    }

    @Override
    public String getLongDescription() {
        return "Used to clear the cache of the UserManager or ServerManager class\n\n Use ?admin utils clear-cache [server, user, all]";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (tokens.isEmpty()) {
            channel.sendMessage("Clear what cache?").queue();
        } else {
            String type = tokens.remove(0).toLowerCase();
            switch (type) {
                case "server":
                    ServerManager.getInstance().clear();
                    channel.sendMessage("Server cache cleared").queue();
                    break;
                case "user":
                    UserManager.getInstance().clear();
                    channel.sendMessage("User cache cleared").queue();
                    break;
                case "all":
                    ServerManager.getInstance().clear();
                    UserManager.getInstance().clear();
                    channel.sendMessage("All caches cleared").queue();
                    break;
                default:
                    channel.sendMessage("No valid action supplied").queue();
                    break;
            }
        }
    }
}
