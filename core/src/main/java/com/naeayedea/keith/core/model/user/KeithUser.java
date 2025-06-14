package com.naeayedea.keith.core.model.user;

import com.naeayedea.keith.core.commands.lib.AccessLevel;

import java.time.Instant;
import java.util.Locale;

public interface KeithUser {

    Instant getFirstSeen();

    String getId();

    AccessLevel getAccessLevel();

    boolean isBanned();

    long getCommandCount();

    Locale getLocale();

    boolean hasPermission(AccessLevel commandLevel);

    String toString();

}
