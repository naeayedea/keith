package com.naeayedea.keith.platform.discord.command.lib.input;

import com.naeayedea.keith.core.commands.lib.input.CommandInput;
import com.naeayedea.keith.core.model.channel.KeithChannel;
import com.naeayedea.keith.core.model.channel.KeithMessageChannel;
import com.naeayedea.keith.core.model.user.KeithUser;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public class DiscordCommandInput implements CommandInput{

    private final Instant messageTimestamp;

    private final KeithUser user;

    private final KeithChannel channel;

    public DiscordCommandInput(KeithUser user, KeithChannel channel, Instant messageTimestamp) {
        this.user = user;
        this.channel = channel;
        this.messageTimestamp = messageTimestamp;
    }

    @Override
    public KeithUser getUser() {
        return user;
    }

    @Override
    public Optional<KeithChannel> getChannel() {
        return Optional.of(channel);
    }

    @Override
    public Instant getTimestamp() {
        return messageTimestamp;
    }

    @Override
    public Locale getLocale() {
        return user.getLocale();
    }

    public static DiscordCommandInput.Builder builder() {
        return new DiscordCommandInput.Builder();
    }

    public static class Builder {

        private Instant messageTimestamp;

        private KeithUser user;

        private KeithChannel channel;

        public Builder timestamp(Instant timestamp) {
            this.messageTimestamp = timestamp;

            return this;
        }

        public Builder user(KeithUser user) {
            this.user = user;

            return this;
        }

        public Builder channel(KeithChannel channel) {
            this.channel = channel;

            return this;
        }

        public DiscordCommandInput build() {
            if (user == null || channel == null) {
                throw new IllegalStateException("You must provide a user and a channel");
            }

            if (messageTimestamp == null) {
                this.messageTimestamp = Instant.now();
            }

            return new DiscordCommandInput(user, channel, messageTimestamp);
        }
    }
}
