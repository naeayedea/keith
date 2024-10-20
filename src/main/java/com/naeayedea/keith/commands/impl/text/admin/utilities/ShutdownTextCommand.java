package com.naeayedea.keith.commands.impl.text.admin.utilities;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShutdownTextCommand extends AbstractOwnerTextCommand {

    private final ApplicationContext applicationContext;

    public ShutdownTextCommand(@Value("${keith.commands.admin.utilities.shutdown.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.shutdown.aliases}', ',')}") List<String> commandAliases, ApplicationContext applicationContext) {
        super(defaultName, commandAliases);
        this.applicationContext = applicationContext;
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"shut down the bot completely\"";
    }

    @Override
    public String getDescription() {
        return "Will shutdown the bot and terminate all processes without restarting";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        event.getChannel().sendMessage("Goodbye").queue(success -> System.exit(0));
    }
}
