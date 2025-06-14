package com.naeayedea.keith.core.model.server;

import com.naeayedea.keith.core.model.channel.KeithChannel;
import com.naeayedea.keith.core.model.message.KeithMessage;
import com.naeayedea.keith.core.model.user.KeithUser;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.List;
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

    @NonNull
    List<KeithChannel> getChannels();

    @NonNull
    List<? extends KeithUser> getUsers();

}
