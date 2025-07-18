package com.naeayedea.keith.common.model.user;

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
