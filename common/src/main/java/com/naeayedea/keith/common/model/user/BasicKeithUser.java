package com.naeayedea.keith.common.model.user;

import java.time.Instant;
import java.util.Locale;

public class BasicKeithUser implements KeithUser {

    private final String userId;

    private final AccessLevel accessLevel;

    private final Instant firstSeen;

    private final long commandCount;

    private final Locale locale;

    public BasicKeithUser(String userId, AccessLevel accessLevel, Instant firstSeen, long commandCount, Locale locale) {
        this.userId = userId;
        this.accessLevel = accessLevel;
        this.firstSeen = firstSeen;
        this.commandCount = commandCount;
        this.locale = locale;
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public String getId() {
        return userId;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public boolean isBanned() {
        return accessLevel == AccessLevel.BANNED;
    }

    public long getCommandCount() {
        return commandCount;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    public boolean hasPermission(AccessLevel commandLevel) {
        return this.accessLevel.num >= commandLevel.num;
    }

    public String toString() {
        return userId + " " + accessLevel + ", " + firstSeen + ", " + commandCount;
    }

}