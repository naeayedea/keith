package com.naeayedea.keith.common.model.command.input;

import com.naeayedea.keith.common.model.channel.KeithChannel;
import com.naeayedea.keith.common.model.user.KeithUser;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public interface CommandInput {

    KeithUser getUser();

    Instant getTimestamp();

    Optional<KeithChannel> getChannel();

    Locale getLocale();
}
