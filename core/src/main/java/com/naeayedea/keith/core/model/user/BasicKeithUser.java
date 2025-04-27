package com.naeayedea.keith.core.model.user;

import com.naeayedea.keith.core.commands.AccessLevel;

import java.time.Instant;

public class BasicKeithUser implements KeithUser {

    private final String userId;

    private final AccessLevel accessLevel;

    private final Instant firstSeen;

    private final long commandCount;

    public BasicKeithUser(String userId, AccessLevel accessLevel, Instant firstSeen, long commandCount) {
        this.userId = userId;
        this.accessLevel = accessLevel;
        this.firstSeen = firstSeen;
        this.commandCount = commandCount;
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
        return accessLevel == AccessLevel.ALL;
    }

    public long getCommandCount() {
        return commandCount;
    }

    public boolean hasPermission(AccessLevel commandLevel) {
        return this.accessLevel.num >= commandLevel.num;
    }

    public String toString() {
        return userId + " " + accessLevel + ", " + firstSeen + ", " + commandCount;
    }

}