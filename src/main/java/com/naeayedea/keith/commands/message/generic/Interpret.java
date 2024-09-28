package com.naeayedea.keith.commands.message.generic;

import com.naeayedea.keith.commands.message.generic.lox.Lox;
import com.naeayedea.keith.util.Utilities;
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
        return prefix + getDefaultName() + super.getShortDescription(prefix);
    }

    @Override
    public String getLongDescription() {
        return "Runs commands in an interpreted language known as Lox.";
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

        String commandInput = Utilities.stringListToString(tokens).trim();

        if (!commandInput.contains("\n") && commandInput.charAt(commandInput.length() - 1) != ';') {
           commandInput += ";";
        }

        List<String> results = (new Lox()).run(commandInput);

        if (results.isEmpty()) {
            event.getChannel().sendMessage("<System> No output, use print to return any output.").queue();
        } else {
            event.getChannel().sendMessage(String.join("\n", results)).queue();
        }
    }
}
