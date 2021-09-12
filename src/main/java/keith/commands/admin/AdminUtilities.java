package keith.commands.admin;

import keith.commands.AccessLevel;
import keith.util.Database;
import keith.util.Utilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class AdminUtilities extends AdminCommand {

    String defaultName;

    public AdminUtilities() {
        defaultName = "utils";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \":eyes:\"";
    }

    @Override
    public String getLongDescription() {
        return ":eyes: nunaya";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (!tokens.isEmpty()) {
            String command = tokens.remove(0);
            switch (command) {
                case "kill":
                    channel.sendMessage("Goodbye").queue(success -> System.exit(0));
                    break;
                case "restart":
                    Utilities.restart(event);
                    break;
                case "find":
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
                    } else {
                        channel.sendMessage("Insufficient Arguments").queue();
                    }
                    break;
                case "uptime":
                    channel.sendMessage(Utilities.getUptimeString()).queue();
                    break;
                case "db": case "database":
                    if (!tokens.isEmpty()) {
                        String result = Database.executeQuery(Utilities.stringListToString(tokens));
                        if (result.length() > 2000) {
                            result = Database.executeQuery(Utilities.stringListToString(tokens)).substring(0, 1900)+"```......";
                        }
                        channel.sendMessage(result).queue();
                    }
                    break;
            }
        }
    }
}
