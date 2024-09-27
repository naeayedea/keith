package com.naeayedea.keith.commands.message.generic;

import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.model.Server;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SetPrefix extends AbstractUserCommand {

    private final int limit;

    private final ServerManager serverManager;

    public SetPrefix(ServerManager serverManager, @Value("${keith.commands.setPrefix.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.setPrefix.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);

        this.serverManager = serverManager;

        this.limit = 10;
    }


    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"sets the prefix of the bot in your server, for prefix limits do "+prefix+"help setprefix!\"";
    }

    @Override
    public String getLongDescription() {
        return "Default prefix clashing with other bots? use setprefix to set a new one! Prefix must be ascii characters excluding spaces and must be less than "+limit+" characters";
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        if (tokens.isEmpty()) {
            event.getChannel().sendMessage("Please enter a prefix, note that it can't contain spaces or non-ascii characters or be longer than "+limit+" characters!").queue();
            return;
        }

        String newPrefix = tokens.getFirst().trim().toLowerCase();

        if (tokens.size() > 1 || containsInvalidCharacters(newPrefix) || newPrefix.length() > limit) {
            event.getChannel().sendMessage("Prefix can't contain spaces or non-ascii characters or be longer than "+limit+" characters!").queue();
        } else {
            Server server = serverManager.getServer(event.getGuild().getId());

            if (server.setPrefix(newPrefix)) {
                event.getChannel().sendMessage("Prefix updated successfully to: '"+server.getPrefix()+"'").queue();
            }  else {
                event.getChannel().sendMessage("Could not set prefix, please contact bot owner").queue();
            }
        }
    }


    //This function checks ensures that the characters within the new prefix are within the desired range of ascii characters
    public boolean containsInvalidCharacters(String token) {
        return token.chars().mapToObj(c -> (char) c).map(c -> c > 32 && c < 127).anyMatch(b -> !b);
    }
}
