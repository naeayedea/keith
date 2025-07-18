package com.naeayedea.keith.common.model.message;

import com.naeayedea.keith.common.model.channel.KeithMessageChannel;
import com.naeayedea.keith.common.model.user.KeithUser;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class EphemeralKeithMessage implements KeithMessage {

    private final KeithUser author;


    private final String messageContent;

    private List<String> messageTokens;

    private final List<KeithUser> mentionedUsers;

    private final Instant timestamp;

    public EphemeralKeithMessage(@NonNull KeithUser author, @NonNull String messageContent, @NonNull List<KeithUser> mentionedUsers, @NonNull Instant timestamp) {
        this.author = author;
        this.messageContent = messageContent;
        this.mentionedUsers = mentionedUsers;
        this.timestamp = timestamp;
    }

    @Override
    @NonNull
    public KeithUser getAuthor() {
        return author;
    }

    @Override
    public Optional<KeithMessageChannel> getChannel() {
        return Optional.empty();
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
        return Optional.empty();
    }
}
