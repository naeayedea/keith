package com.naeayedea.keith.platform.discord.model.message;

import com.naeayedea.keith.common.model.channel.KeithMessageChannel;
import com.naeayedea.keith.common.model.message.KeithMessage;
import com.naeayedea.keith.common.model.user.KeithUser;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DiscordKeithMessage implements KeithMessage {

    private final KeithUser user;

    private final KeithMessageChannel channel;

    private final Message message;

    private final List<KeithUser> mentionedUsers;

    private final KeithMessage repliedTo;

    private List<String> messageContentAsTokens = null;

    public DiscordKeithMessage(@NonNull KeithUser user, @NonNull KeithMessageChannel channel, @NonNull Message message, @NonNull List<KeithUser> mentionedUsers, KeithMessage repliedTo) {
        this.user = user;
        this.channel = channel;
        this.message = message;
        this.mentionedUsers = mentionedUsers;
        this.repliedTo = repliedTo;
    }

    @Override
    public KeithUser getAuthor() {
        return user;
    }

    @Override
    public Optional<KeithMessageChannel> getChannel() {
        return Optional.of(channel);
    }

    @Override
    public String getMessageAsContent() {
        return message.getContentRaw();
    }

    @Override
    public List<String> getMessageAsTokens() {
        if (messageContentAsTokens == null) {
            messageContentAsTokens = Arrays.asList(getMessageAsContent().trim().split("\\s+"));
        }

        return new ArrayList<>(messageContentAsTokens);
    }

    @Override
    public List<KeithUser> mentionedUsers() {
        return new ArrayList<>(mentionedUsers);
    }

    @Override
    public Instant getTimestamp() {
        return message.getTimeCreated().toInstant();
    }

    @Override
    @NonNull
    public Optional<KeithMessage> getRepliedTo() {
        return repliedTo == null ? Optional.empty() : Optional.of(repliedTo);
    }

    public static DiscordKeithMessage.Builder builder() {
        return new DiscordKeithMessage.Builder();
    }

    public static class Builder {
        private KeithUser user;
        private KeithMessageChannel channel;
        private Message message;
        private List<KeithUser> mentionedUsers;
        private KeithMessage repliedTo;

        public DiscordKeithMessage.Builder user(KeithUser user) {
            this.user = user;

            return this;
        }

        public DiscordKeithMessage.Builder channel(KeithMessageChannel channel) {
            this.channel = channel;

            return this;
        }

        public DiscordKeithMessage.Builder message(Message message) {
            this.message = message;

            return this;
        }

        public DiscordKeithMessage.Builder mentionedUsers(List<KeithUser> mentionedUsers) {
            this.mentionedUsers = mentionedUsers;

            return this;
        }

        public DiscordKeithMessage.Builder repliedTo(KeithMessage repliedTo) {
            this.repliedTo = repliedTo;

            return this;
        }

        public DiscordKeithMessage build() {
            if (user == null) {
                throw new IllegalStateException("Discord user not set");
            }

            if (channel == null) {
                throw new IllegalStateException("Discord message channel not set");
            }

            if (mentionedUsers == null) {
                mentionedUsers = new ArrayList<>();
            }

            return new DiscordKeithMessage(user, channel, message, mentionedUsers, repliedTo);
        }
    }
}
