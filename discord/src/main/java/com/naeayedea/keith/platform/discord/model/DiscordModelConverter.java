package com.naeayedea.keith.platform.discord.model;

import com.naeayedea.keith.core.managers.cache.KeithMessageChannelCache;
import com.naeayedea.keith.core.managers.KeithServerManager;
import com.naeayedea.keith.core.managers.KeithUserManager;
import com.naeayedea.keith.core.model.channel.KeithMessageChannel;
import com.naeayedea.keith.core.model.message.KeithMessage;
import com.naeayedea.keith.core.model.user.KeithUser;
import com.naeayedea.keith.platform.discord.utils.KeithDiscordConstants;
import com.naeayedea.keith.platform.discord.model.channel.DiscordKeithMessageChannel;
import com.naeayedea.keith.platform.discord.model.message.DiscordKeithMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DiscordModelConverter {

    private final KeithUserManager userManager;

    private final KeithServerManager serverManager;

    private final KeithMessageChannelCache messageChannelCache;

    private static final int MAX_REPLIED_TO_DEPTH = 5;

    public DiscordModelConverter(KeithUserManager userManager, KeithServerManager serverManager, KeithMessageChannelCache messageChannelCache) {
        this.userManager = userManager;
        this.serverManager = serverManager;
        this.messageChannelCache = messageChannelCache;
    }

    @NonNull
    public KeithMessage getKeithMessageFromDiscordMessage(@NonNull Message message) {
        return getKeithMessageFromDiscordMessage(message, 0);
    }

    @NonNull
    private KeithMessage getKeithMessageFromDiscordMessage(@NonNull Message message, int repliedToDepth) {
        KeithMessage repliedTo = null;

        if (repliedToDepth < MAX_REPLIED_TO_DEPTH && message.getReferencedMessage() != null) {
            repliedTo = getKeithMessageFromDiscordMessage(message.getReferencedMessage(), repliedToDepth + 1);
        }

        return DiscordKeithMessage.builder()
            .message(message)
            .channel(getKeithMessageChannelFromDiscordMessageChannel(message.getChannel()))
            .user(getKeithUser(message.getAuthor().getId()))
            .repliedTo(repliedTo)
            .mentionedUsers(message.getMentions().getUsers().stream().map(user -> getKeithUser(user.getId())).toList())
            .build();
    }

    @NonNull
    public KeithMessageChannel getKeithMessageChannelFromDiscordMessageChannel(@NonNull MessageChannel channel) {
        KeithMessageChannel keithChannel = messageChannelCache.getChannel(channel.getId(), KeithDiscordConstants.PLATFORM_NAME);

        //if the channel is in the cache, return it
        if (keithChannel != null) {
            return keithChannel;
        }

        //otherwise create and put the channel in the cache
        return messageChannelCache.putChannel(new DiscordKeithMessageChannel(channel, this));
    }

    @NonNull
    private KeithUser getKeithUser(@NonNull String userId) {
        return userManager.getUser(userId);
    }
}
