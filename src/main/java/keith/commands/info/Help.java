package keith.commands.info;

import keith.commands.AccessLevel;
import keith.commands.Command;
import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Help extends InfoCommand {

    private final Map<String, Command> commands;
    private final String defaultName;

    public Help (Map<String, Command> commandMap, String defaultName) {
        this.commands = commandMap;
        this.defaultName = defaultName;
    }


    @Override
    public String getShortDescription(String prefix) {
        return prefix+"help: \"for more information on a command use "+prefix+"help [command]\"";
    }

    @Override
    public String getLongDescription() {
        return "Help lists all available commands as well as going into further detail when help on a specific command" +
                "is requested using one of its aliases";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        String prefix = Utilities.getPrefix(event);
        if(tokens.isEmpty()){
            sendAllCommandHelp(event.getChannel(), prefix);
        } else {
            sendSingleCommandHelp(event.getChannel(), tokens.get(0));
        }
    }

    public void sendAllCommandHelp(MessageChannel channel, String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Help");
        eb.setColor(Utilities.getDefaultColor());
        eb.setDescription("For information on specific commands do \""+prefix+"help [command\"");
        StringBuilder helpString = new StringBuilder("```cs\n");
        for (Command command : commands.values()) {
            String description = command.getShortDescription(prefix);
            //only add description if not duplicate (need to look into improving multimap, maybe have two maps?
            if (helpString.indexOf(description) == -1) {
                helpString.append(command.getShortDescription(prefix)).append("\n");
            }
        }
        helpString.append("```");
        eb.addField("List of Commands", helpString.toString(), false);
        channel.sendMessage(eb.build()).queue();
    }

    public void sendSingleCommandHelp(MessageChannel channel, String commandString) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Utilities.getDefaultColor());
        Command command = commands.get(commandString);
        if (command != null) {
            eb.setTitle("Help");
            StringBuilder knownAliases = new StringBuilder();
            commands.forEach((key, val) -> {if(val.equals(command)){knownAliases.append(key).append(", ");}});
            eb.setDescription("Extended information on "+command.getDefaultName());
            eb.addField("Known Aliases", knownAliases.substring(0, knownAliases.length()-2), false);
            eb.addField("Information", command.getLongDescription(), false);
            channel.sendMessage(eb.build()).queue();
        } else {
            channel.sendMessage("No command found with that name or alias").queue();
        }


    }
}
