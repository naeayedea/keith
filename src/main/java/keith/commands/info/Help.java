package keith.commands.info;

import keith.commands.Command;
import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;

public class Help extends InfoCommand {

    private final Map<String, Command> commands;
    private final String defaultName;

    public Help (Map<String, Command> commandMap) {
        this.commands = commandMap;
        this.defaultName = "help";
    }


    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"for more information on a command use "+prefix+"help [command]\"";
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
            sendSingleCommandHelp(event.getChannel(), tokens.get(0).toLowerCase());
        }
    }

    public void sendAllCommandHelp(MessageChannel channel, String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Help");
        eb.setColor(Utilities.getBotColor());
        eb.setDescription("For information on specific commands do \""+prefix+"help [command]\"");
        StringBuilder helpString = new StringBuilder("```cs\n");
        for (Command command : commands.values()) {
            String description = command.getShortDescription(prefix);
            //only add description if not duplicate (need to look into improving multimap, maybe have two maps?
            if (helpString.indexOf(description) == -1 && !command.isHidden()) {
                helpString.append(command.getShortDescription(prefix)).append("\n");
            }
        }
        helpString.append("```");
        eb.addField("List of Commands", helpString.toString(), false);
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void sendSingleCommandHelp(MessageChannel channel, String commandString) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Utilities.getBotColor());
        Command command = commands.get(commandString);
        if (command != null) {
            eb.setTitle("Command: "+command.getDefaultName());
            StringBuilder knownAliases = new StringBuilder();
            commands.forEach((key, val) -> {
                //for every command, if command matches the requested command, add its aliases to the embed
                //do not add the default name.
                if(val.equals(command) && !key.equals(val.getDefaultName())) {
                    knownAliases.append(key).append(", ");}
            });
            eb.setDescription("Below is extended information on the command " + command.getDefaultName() + " and its known aliases");
            if (knownAliases.toString().trim().equals("")) {
                eb.addField("Known Aliases", "No aliases", false);
            } else {
            eb.addField("Known Aliases", knownAliases.substring(0, knownAliases.length()-2), false);
            }
            eb.addField("Information", command.getLongDescription(), false);
            channel.sendMessageEmbeds(eb.build()).queue();
        } else {
            channel.sendMessage("No command found with that name or alias").queue();
        }


    }
}
