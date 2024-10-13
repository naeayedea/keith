package com.naeayedea.keith.commands.message.admin.utilities;

import com.naeayedea.keith.util.Database;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseSearch extends AbstractOwnerCommand {

    private final Database database;

    public DatabaseSearch(@Value("${keith.commands.admin.utilities.databaseSearch.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.databaseSearch.aliases}', ',')}") List<String> commandAliases, Database database) {
        super(defaultName, commandAliases);
        this.database = database;
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"use the database\"";
    }

    @Override
    public String getDescription() {
        return "Used for interacting with the database directly - be careful";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (!tokens.isEmpty()) {
            String result = database.executeQuery(Utilities.stringListToString(tokens));
            if (result.length() > 2000) {
                result = database.executeQuery(Utilities.stringListToString(tokens)).substring(0, 1900) + "```......";
            }
            channel.sendMessage(result).queue();
        }
    }
}
