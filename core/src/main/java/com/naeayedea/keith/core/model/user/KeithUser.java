package com.naeayedea.keith.core.model.user;

import com.naeayedea.keith.core.commands.AccessLevel;

import java.time.Instant;

public interface KeithUser {

    Instant getFirstSeen();

    String getId();

    AccessLevel getAccessLevel();

    boolean isBanned();

    long getCommandCount();

    boolean hasPermission(AccessLevel commandLevel);

    String toString();

}
