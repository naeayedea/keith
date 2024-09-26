package com.naeayedea.model.chat;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class ChatAgent {

    private final MessageChannel channel;

    private final boolean isFeedback;

    public ChatAgent(MessageChannel linkedChannel, boolean isFeedback) {
        this.channel = linkedChannel;
        this.isFeedback = isFeedback;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public boolean isFeedback() {
        return isFeedback;
    }
}
