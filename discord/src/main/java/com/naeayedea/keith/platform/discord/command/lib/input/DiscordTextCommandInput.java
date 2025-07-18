package com.naeayedea.keith.platform.discord.command.lib.input;

import com.naeayedea.keith.common.model.command.input.TextCommandInput;
import com.naeayedea.keith.common.model.channel.KeithChannel;
import com.naeayedea.keith.common.model.channel.KeithMessageChannel;
import com.naeayedea.keith.common.model.message.KeithMessage;
import com.naeayedea.keith.common.model.user.KeithUser;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public class DiscordTextCommandInput implements TextCommandInput {

    private final KeithUser user;

    private final KeithMessage message;

    private final Instant timestamp;

    public DiscordTextCommandInput(@NonNull KeithUser user, @NonNull KeithMessage message, @NonNull Instant timestamp) {
        this.user = user;
        this.message = message;
        this.timestamp = timestamp;
    }

    @Override
    @NonNull
    public KeithMessage getMessage() {
        return message;
    }

    @Override
    @NonNull
    public KeithUser getUser() {
        return user;
    }

    @Override
    @NonNull
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    @NonNull
    public Optional<KeithChannel> getChannel() {
        Optional<KeithMessageChannel> messageChannel = message.getChannel();

        //noinspection OptionalIsPresent
        if (messageChannel.isPresent()) {
            return Optional.of(messageChannel.get());
        }

        return Optional.empty();
    }

    @Override
    @NonNull
    public Locale getLocale() {
        return user.getLocale();
    }

    @NonNull
    public static DiscordTextCommandInput.Builder builder() {
        return new DiscordTextCommandInput.Builder();
    }

    public static class Builder {

        private Instant messageTimestamp;

        private KeithUser user;

        private KeithMessage message;

        public DiscordTextCommandInput.Builder timestamp(Instant timestamp) {
            this.messageTimestamp = timestamp;

            return this;
        }

        public DiscordTextCommandInput.Builder user(KeithUser user) {
            this.user = user;

            return this;
        }

        public DiscordTextCommandInput.Builder message(KeithMessage message) {
            this.message = message;

            return this;
        }

        public DiscordTextCommandInput build() {
            if (user == null || message == null) {
                throw new IllegalStateException("You must provide a user and a channel");
            }

            if (messageTimestamp == null) {
                this.messageTimestamp = Instant.now();
            }

            return new DiscordTextCommandInput(user, message, messageTimestamp);
        }
    }
}
