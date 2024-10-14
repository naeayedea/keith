package com.naeayedea.keith.commands.text.info;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Invite extends AbstractInfoCommand {

    public Invite(@Value("${keith.commands.invite.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.invite.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"Invite me to your other servers!\"";
    }

    @Override
    public String getDescription() {
        return "sends a link which can be used to invite keith to other servers!";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        Utilities.Messages.sendEmbed(event.getChannel(), "Invite Me!", "[https://keithbot.com/invite](https://discord.com/oauth2/authorize?client_id=624702573064224803&scope=bot&permissions=1126899292376176)", event);
    }
}
