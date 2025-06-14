package com.naeayedea.keith.core.model.message;

import com.naeayedea.keith.core.model.channel.KeithMessageChannel;
import com.naeayedea.keith.core.model.user.KeithUser;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class BasicKeithMessage implements KeithMessage {

    private final KeithUser author;

    private final KeithMessageChannel channel;

    private final String messageContent;

    private List<String> messageTokens;

    private final List<KeithUser> mentionedUsers;

    private final Instant timestamp;

    private final KeithMessage repliedTo;

    public BasicKeithMessage(@NonNull KeithUser author, @NonNull KeithMessageChannel channel, @NonNull String messageContent, @NonNull List<KeithUser> mentionedUsers, @NonNull Instant timestamp) {
        this.author = author;
        this.channel = channel;
        this.messageContent = messageContent;
        this.mentionedUsers = mentionedUsers;
        this.timestamp = timestamp;
        this.repliedTo = null;
    }

    public BasicKeithMessage(@NonNull KeithUser author, @NonNull KeithMessageChannel channel, @NonNull String messageContent, @NonNull List<KeithUser> mentionedUsers, @NonNull Instant timestamp, KeithMessage repliedTo) {
        this.author = author;
        this.channel = channel;
        this.messageContent = messageContent;
        this.mentionedUsers = mentionedUsers;
        this.timestamp = timestamp;
        this.repliedTo = repliedTo;
    }

    @Override
    @NonNull
    public KeithUser getAuthor() {
        return author;
    }

    @Override
    public Optional<KeithMessageChannel> getChannel() {
        return Optional.of(channel);
    }

    @Override
    @NonNull
    public String getMessageAsContent() {
        return messageContent;
    }

    @Override
    @NonNull
    public List<String> getMessageAsTokens() {
        if (messageTokens == null) {
            messageTokens = List.of(messageContent.split("\\S+"));
        }

        return messageTokens;
    }

    @Override
    @NonNull
    public List<KeithUser> mentionedUsers() {
        return mentionedUsers;
    }

    @Override
    @NonNull
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    @NonNull
    public Optional<KeithMessage> getRepliedTo() {
        return repliedTo != null ? Optional.of(repliedTo) : Optional.empty();
    }
}
