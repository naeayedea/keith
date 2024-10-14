package com.naeayedea.keith.commands.text.admin;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SetStatus extends AbstractAdminTextCommand {

    public SetStatus(@Value("${keith.commands.admin.setStatus.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.setStatus.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"sets the bots status to the specified message\"";
    }

    @Override
    public String getDescription() {
        return "sets the bot status to the specified message, can also do \"" +
            "setstatus default\" to return the bot status to the default setting";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        if (tokens.size() == 1 && tokens.getFirst().equalsIgnoreCase("default")) {
            Utilities.forceDefaultStatus();
        } else {
            Utilities.setStatus(Utilities.stringListToString(tokens));
        }
    }
}
