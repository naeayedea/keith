package com.naeayedea.keith.commands.message.admin.utilities;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Shutdown extends AbstractOwnerCommand {

    public Shutdown(@Value("${keith.commands.admin.utilities.shutdown.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.shutdown.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix + getDefaultName() + ": \"shut down the bot completely\"";
    }

    @Override
    public String getLongDescription() {
        return "Will shutdown the bot and terminate all processes without restarting";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        event.getChannel().sendMessage("Goodbye").queue(success -> Utilities.runShutdownProcedure());
    }
}
