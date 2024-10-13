package com.naeayedea.keith.model;

import com.naeayedea.keith.commands.message.AccessLevel;
import com.naeayedea.keith.util.Utilities;

public class Candidate {

    private final String discordID;

    private final AccessLevel accessLevel;

    private final String firstSeen;

    private final long commandCount;

    public Candidate(String discordID, AccessLevel accessLevel, String firstSeen, long commandCount) {
        this.discordID = discordID;
        this.accessLevel = accessLevel;
        this.firstSeen = firstSeen;
        this.commandCount = commandCount;
    }

    public String getFirstSeen() {
        return firstSeen;
    }

    public String getId() {
        return discordID;
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
        return discordID + " " + accessLevel + ", " + firstSeen + ", " + commandCount;
    }

    public String getAsMention() {
        return "<@!" + this.discordID + ">";
    }

    public String getDescription() {
        return this.toString();
    }


}