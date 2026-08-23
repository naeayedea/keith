package com.naeayedea.keith.common.model.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Locale;

public class BasicKeithUser implements KeithUser {

    private final String userId;

    private final AccessLevel accessLevel;

    private final Instant firstSeen;

    private final long commandCount;

    private final Locale locale;

    @JsonCreator
    public BasicKeithUser(@JsonProperty("id") String userId, @JsonProperty("accessLevel") AccessLevel accessLevel, @JsonProperty("firstSeen") Instant firstSeen, @JsonProperty("commandCount") long commandCount, @JsonProperty("locale") Locale locale) {
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