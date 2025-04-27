package com.naeayedea.keith.core.model.server;


import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Optional;

public class BasicKeithServer implements KeithServer {

    private final String serverID;

    private final Instant firstSeen;

    private final String prefix;

    private final Boolean isBanned;

    private final String pinChannelId;

    public BasicKeithServer(@NonNull String serverID, @NonNull Instant firstSeen, @NonNull String prefix, @NonNull Boolean isBanned, String pinChannelId) {
        this.serverID = serverID;
        this.firstSeen = firstSeen;
        this.prefix = prefix;
        this.isBanned = isBanned;
        this.pinChannelId = pinChannelId;
    }

    public String toString() {
        return "<" + serverID + "> First Seen: " + firstSeen + ", Prefix: " + prefix;
    }


    @Override
    @NonNull
    public String getServerId() {
        return serverID;
    }

    @Override
    @NonNull
    public Instant getFirstSeen() {
        return firstSeen;
    }

    @Override
    @NonNull
    public String getPrefix() {
        return prefix;
    }

    @Override
    @NonNull
    public Boolean isBanned() {
        return isBanned;
    }

    @Override
    @NonNull
    public Optional<String> getPinChannelId() {
        return pinChannelId != null ? Optional.of(pinChannelId) : Optional.empty();
    }
}