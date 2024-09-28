package com.naeayedea.keith.commands.message.admin.utilities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Locate extends AbstractAdminUtilsCommand {

    public Locate(@Value("${keith.commands.admin.utilities.locate.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.locate.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix + getDefaultName() + ": \"Find a user or server\"";
    }

    @Override
    public String getLongDescription() {
        return "Enter the ID of a user or server to locate it within the database";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (tokens.size() > 1) {
            String type = tokens.removeFirst();
            String id = tokens.removeFirst();
            if (type.equals("server")) {
                Guild server = event.getJDA().getGuildById(id);
                if (server != null) {
                    channel.sendMessage(server.toString()).queue();
                } else {
                    channel.sendMessage("Could not find server").queue();
                }
            } else if (type.equals("user")) {
                User user = event.getJDA().getUserById(id);
                if (user != null) {
                    channel.sendMessage(user.getName() + "#" + user.getDiscriminator()).queue();
                } else {
                    channel.sendMessage("Could not find user").queue();
                }
            } else {
                channel.sendMessage("No search param provided, usage: server <id> or user <id>").queue();
            }
        }
    }
}
