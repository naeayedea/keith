package com.naeayedea.keith.model.chat;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class ChatCandidate {
    private final Guild guild;

    private final MessageChannel channel;

    private final long connectionTime;

    public ChatCandidate(MessageChannel channel, Guild guild, long connectionTime) {
        this.channel = channel;
        this.guild = guild;
        this.connectionTime = connectionTime;
    }

    public Guild getGuild() {
        return guild;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public long getConnectionTime() {
        return connectionTime;
    }
}
