package com.naeayedea.keith.commands.impl.text.generic;

import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.model.Server;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
public class SetPrefixCommand extends AbstractUserCommand {

    private final int limit;

    private final ServerManager serverManager;

    public SetPrefixCommand(ServerManager serverManager, @Value("${keith.commands.setPrefix.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.setPrefix.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);

        this.serverManager = serverManager;

        this.limit = 10;
    }


    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"sets the prefix of the bot in your server, for prefix limits do " + prefix + "help setprefix!\"";
    }

    @Override
    public String getDescription() {
        return "Default prefix clashing with other bots? use setprefix to set a new one! Prefix must be ascii characters excluding spaces and must be less than " + limit + " characters";
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) throws KeithExecutionException {
        if (tokens.isEmpty()) {
            event.getChannel().sendMessage("Please enter a prefix, note that it can't contain spaces or non-ascii characters or be longer than " + limit + " characters!").queue();
            return;
        }

        String newPrefix = tokens.getFirst().trim().toLowerCase();

        if (tokens.size() > 1 || containsInvalidCharacters(newPrefix) || newPrefix.length() > limit) {
            event.getChannel().sendMessage("Prefix can't contain spaces or non-ascii characters or be longer than " + limit + " characters!").queue();
        } else {
            Server server = serverManager.getServer(event.getGuild().getId());

            try {
                if (serverManager.setPrefix(server.serverID(), newPrefix).prefix().equals(newPrefix)) {
                    event.getChannel().sendMessage("Prefix updated successfully to: '" + server.prefix() + "'").queue();
                } else {
                    throw new KeithExecutionException("Could not set prefix, please contact bot owner");
                }
            } catch (SQLException e) {
                throw new KeithExecutionException("Could not set prefix, please contact bot owner");
            }

        }
    }


    //This function checks ensures that the characters within the new prefix are within the desired range of ascii characters
    public boolean containsInvalidCharacters(String token) {
        return token.chars().mapToObj(c -> (char) c).map(c -> c > 32 && c < 127).anyMatch(b -> !b);
    }
}
