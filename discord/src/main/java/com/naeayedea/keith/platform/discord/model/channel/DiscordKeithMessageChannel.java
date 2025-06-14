package com.naeayedea.keith.platform.discord.model.channel;


import com.naeayedea.keith.core.model.channel.KeithMessageChannel;
import com.naeayedea.keith.core.model.message.KeithMessage;
import com.naeayedea.keith.platform.discord.utils.KeithDiscordConstants;
import com.naeayedea.keith.platform.discord.model.DiscordModelConverter;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.List;

public class DiscordKeithMessageChannel implements KeithMessageChannel {

    private final MessageChannel channel;

    private final DiscordModelConverter discordModelConverter;

    public DiscordKeithMessageChannel(MessageChannel channel, DiscordModelConverter discordModelConverter) {
        this.channel = channel;
        this.discordModelConverter = discordModelConverter;
    }

    @Override
    public String getId() {
        return channel.getId();
    }

    @Override
    public String getName() {
        return channel.getName();
    }

    @Override
    public String getPlatform() {
        return KeithDiscordConstants.PLATFORM_NAME;
    }

    @Override
    public List<KeithMessage> getLastNMessages(int N) {
        return channel.getHistory().retrievePast(N).complete().stream().map(discordModelConverter::getKeithMessageFromDiscordMessage).toList();
    }

}
