package com.naeayedea.keith.commands.impl.text.admin;

import com.naeayedea.keith.exception.KeithGracefulErrorException;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SendMessageCommand extends AbstractAdminCommand {

    public SendMessageCommand(@Value("${keith.commands.admin.sendMessage.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.sendMessage.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @NotNull
    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"lets you send a message to another channel\"";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "lets you send a message to another channel - use \"send message/embed [channelid] [title(embed only] [message]\"";
    }

    @Override
    public void run(@NotNull MessageReceivedEvent event, @NotNull List<String> tokens) throws KeithGracefulErrorException {
        MessageChannel channel = event.getChannel();
        try {
            String type = tokens.removeFirst();
            switch (type) {
                case "message": {
                    String channelId = tokens.removeFirst();
                    String message = Utilities.stringListToString(tokens);

                    MessageChannel destination = event.getJDA().getTextChannelById(channelId);

                    if (destination == null) {
                        throw new KeithGracefulErrorException("Channel unavailable, ensure id is correct.");
                    }

                    destination.sendMessage(message).queue();

                    break;
                }
                case "embed": {
                    String channelId = tokens.removeFirst();
                    String title = tokens.removeFirst();
                    String message = Utilities.stringListToString(tokens);

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(title);
                    embedBuilder.setDescription(message);
                    embedBuilder.setColor(Utilities.getBotColor());

                    MessageChannel destination = event.getJDA().getTextChannelById(channelId);

                    if (destination == null) {
                        throw new KeithGracefulErrorException("Channel unavailable, ensure id is correct.");
                    }

                    destination.sendMessageEmbeds(embedBuilder.build()).queue();

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
                        } catch (PermissionException ignored) {}
                    }
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            channel.sendMessage("Insufficient arguments, see help").queue();
        }
    }
}
