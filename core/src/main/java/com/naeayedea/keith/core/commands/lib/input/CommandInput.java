package com.naeayedea.keith.core.commands.lib.input;

import com.naeayedea.keith.core.model.channel.KeithChannel;
import com.naeayedea.keith.core.model.user.KeithUser;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public interface CommandInput {

    KeithUser getUser();

    Instant getTimestamp();

    Optional<KeithChannel> getChannel();

    Locale getLocale();
}
