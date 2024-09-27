package com.naeayedea.keith.commands.message.admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Echo extends AbstractAdminCommand {

    public Echo(@Value("${keith.commands.admin.echo.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.echo.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"bot simply returns the text included in the command minus the prefix/command\"";
    }

    @Override
    public String getLongDescription() {
        return "Echo command is self explanatory, the bot will echo any text given to it minus the prefix+command";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        StringBuilder response = new StringBuilder();
        tokens.forEach(string -> response.append(string).append(" "));
        event.getChannel().sendMessage(response).queue(success -> {
            event.getMessage().delete().queue();
        });
    }
}
