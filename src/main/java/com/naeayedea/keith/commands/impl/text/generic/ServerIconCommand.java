package com.naeayedea.keith.commands.impl.text.generic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServerIconCommand extends AbstractUserCommand {


    public ServerIconCommand(@Value("${keith.commands.serverIcon.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.serverIcon.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"displays icon of the current guild/server\"";
    }

    @Override
    public String getDescription() {
        return "servericon displays the icon of the current server/guild";
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        EmbedBuilder embed = new EmbedBuilder();
        Guild guild = event.getGuild();
        embed.setTitle("Icon for " + guild.getName());
        embed.setImage(guild.getIconUrl() + "?size=4096");
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
