package keith.commands.admin.utilities;

import keith.util.Database;
import keith.util.Utilities;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class DatabaseSearch extends OwnerCommand {

    public DatabaseSearch() {
        super("database");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"use the database\"";
    }

    @Override
    public String getLongDescription() {
        return "Used for interacting with the database directly - be careful";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (!tokens.isEmpty()) {
            String result = Database.executeQuery(Utilities.stringListToString(tokens));
            if (result.length() > 2000) {
                result = Database.executeQuery(Utilities.stringListToString(tokens)).substring(0, 1900)+"```......";
            }
            channel.sendMessage(result).queue();
        }
    }
}
