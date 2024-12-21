package com.naeayedea.keith.commands.impl.text.info;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InviteCommand extends AbstractInfoCommand {

    public InviteCommand(@Value("${keith.commands.invite.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.invite.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @NotNull
    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"Invite me to your other servers!\"";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "sends a link which can be used to invite keith to other servers!";
    }

    @Override
    public void run(@NotNull MessageReceivedEvent event, @NotNull List<String> tokens) {

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Invite Me!");
        embedBuilder.setDescription("[https://keithbot.com/invite](https://discord.com/oauth2/authorize?client_id=624702573064224803&scope=bot&permissions=1126899292376176)");
        embedBuilder.setColor(Utilities.getBotColor());

        event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
