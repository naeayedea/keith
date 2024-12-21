package com.naeayedea.keith.commands.impl.text.admin.utilities;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UptimeCommand extends AbstractAdminUtilsCommand {

    public UptimeCommand(@Value("${keith.commands.admin.utilities.uptime.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.uptime.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @NotNull
    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"get uptime\"";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Returns the current uptime since last restart or major disconnect";
    }

    @Override
    public void run(@NotNull MessageReceivedEvent event, @NotNull List<String> tokens) {
        event.getChannel().sendMessage(Utilities.getUptimeString()).queue();
    }
}
