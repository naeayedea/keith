package com.naeayedea.keith.commands.text.admin;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SendMessageTextCommand extends AbstractAdminTextCommand {

    public SendMessageTextCommand(@Value("${keith.commands.admin.sendMessage.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.sendMessage.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"lets you send a message to another channel\"";
    }

    @Override
    public String getDescription() {
        return "lets you send a message to another channel - use \"send message/embed [channelid] [title(embed only] [message]\"";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        try {
            String type = tokens.removeFirst();
            switch (type) {
                case "message": {
                    String channelId = tokens.removeFirst();
                    String message = Utilities.stringListToString(tokens);
                    Utilities.Messages.sendMessage(channelId, message, event);
                    break;
                }
                case "embed": {
                    String channelId = tokens.removeFirst();
                    String title = tokens.removeFirst();
                    String message = Utilities.stringListToString(tokens);
                    Utilities.Messages.sendEmbed(channelId, title, message, event);
                    break;
                }
                case "blast": {
                    if (channel instanceof PrivateChannel) {
                        channel.sendMessage("can't use blast in private message").queue();
                        return;
                    }
                    String message = Utilities.stringListToString(tokens);
                    for (MessageChannel messageChannel : event.getGuild().getTextChannels()) {
                        try {
                            messageChannel.sendMessage(message).queue();
                        } catch (PermissionException ignored) {
                        }
                    }
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            channel.sendMessage("Insufficient arguments, see help").queue();
        }
    }
}
