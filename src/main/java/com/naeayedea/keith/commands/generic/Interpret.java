package com.naeayedea.keith.commands.generic;

import com.naeayedea.keith.commands.generic.lox.Lox;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Interpret extends AbstractUserCommand {

    public Interpret(@Value("${keith.commands.interpret.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.interpret.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+super.getShortDescription(prefix);
    }

    @Override
    public String getLongDescription() {
        return super.getLongDescription();
    }

    @Override
    public int getTimeOut() {
        return 15;
    }

    @Override
    public int getCost() {
        return 3;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        String raw = event.getMessage().getContentRaw();
        List<String> results = (new Lox()).run(raw.substring(raw.indexOf('`')));
        event.getChannel().sendMessage(String.join("\n", results)).queue();
    }
}
