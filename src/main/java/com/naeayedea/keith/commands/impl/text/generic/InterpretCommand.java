package com.naeayedea.keith.commands.impl.text.generic;

import com.naeayedea.keith.commands.impl.text.generic.lox.Lox;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterpretCommand extends AbstractUserCommand {

    public InterpretCommand(@Value("${keith.commands.interpret.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.interpret.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + "";
    }

    @Override
    public String getDescription() {
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
