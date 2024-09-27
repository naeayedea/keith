package com.naeayedea.keith.commands.message.admin.utilities;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Restart extends AbstractAdminUtilsCommand {

    public Restart(@Value("${keith.commands.admin.utilities.restart.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.restart.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"restarts the bot\"";
    }

    @Override
    public String getLongDescription() {
        return "Will shutdown and relaunch the bots processes - useful after update";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        Utilities.restart(event);
    }
}
