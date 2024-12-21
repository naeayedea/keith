package com.naeayedea.keith.commands.impl.text.admin.utilities;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShutdownCommand extends AbstractOwnerCommand {

    public ShutdownCommand(@Value("${keith.commands.admin.utilities.shutdown.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.shutdown.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @NotNull
    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"shut down the bot completely\"";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Will shutdown the bot and terminate all processes without restarting";
    }

    @Override
    public void run(@NotNull MessageReceivedEvent event, @NotNull List<String> tokens) {
        event.getChannel().sendMessage("Goodbye").queue(success -> System.exit(0));
    }
}
