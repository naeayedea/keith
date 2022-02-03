package keith.commands.admin.utilities;

import keith.commands.admin.AdminCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Locate extends AdminCommand {

    String defaultName;

    public Locate() {
        defaultName = "locate";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"Find a user or server\"";
    }

    @Override
    public String getLongDescription() {
        return "Enter the ID of a user or server to locate it within the database";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (tokens.size() > 1) {
            String type = tokens.remove(0);
            String id = tokens.remove(0);
            if (type.equals("server")) {
                Guild server = event.getJDA().getGuildById(id);
                if (server != null) {
                    channel.sendMessage(server.toString()).queue();
                } else {
                    channel.sendMessage("Could not find server").queue();
                }
            } else if (type.equals("user")) {
                User user = event.getJDA().getUserById(id);
                if (user != null) {
                    channel.sendMessage(user.getName()+"#"+user.getDiscriminator()).queue();
                } else {
                    channel.sendMessage("Could not find user").queue();
                }
            }
        }
    }
}
