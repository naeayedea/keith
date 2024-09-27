package com.naeayedea.keith.commands.admin.utilities;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Uptime extends AbstractAdminUtilsCommand {

    public Uptime(@Value("${keith.commands.admin.utilities.uptime.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.uptime.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"get uptime\"";
    }

    @Override
    public String getLongDescription() {
        return "Returns the current uptime since last restart or major disconnect";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
       event.getChannel().sendMessage(Utilities.getUptimeString()).queue();
    }
}
