package com.naeayedea.keith.core.model.server;

import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Optional;

public interface KeithServer {

    @NonNull
    String getServerId();

    @NonNull
    Instant getFirstSeen();

    @NonNull
    String getPrefix();

    @NonNull
    Boolean isBanned();

    @NonNull
    Optional<String> getPinChannelId();

}
