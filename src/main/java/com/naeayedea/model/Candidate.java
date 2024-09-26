package com.naeayedea.model;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.util.Database;
import com.naeayedea.keith.util.Utilities;

import java.sql.PreparedStatement;

public class Candidate {
        private final String discordID;
        private AccessLevel accessLevel;
        private final String firstSeen;
        private long commandCount;

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

        public boolean setAccessLevel(AccessLevel accessLevel) {
            if (this.accessLevel != AccessLevel.OWNER) {
                if (Database.executeUpdate(accessLevelStatement(), accessLevel.num, this.discordID)) {
                    this.accessLevel = accessLevel;
                    return true;
                }
            }
            return false;
        }

        public long getCommandCount() {
            return commandCount;
        }

        public void incrementCommandCount() {
            if (Database.executeUpdate(incrementCommandCountStatement(), this.discordID)) {
                this.commandCount++;
            }
        }

        public boolean hasPermission(AccessLevel commandLevel) {
            return this.accessLevel.num >= commandLevel.num;
        }

        public String toString() {
            net.dv8tion.jda.api.entities.User user =  Utilities.getJDAInstance().getUserById(this.discordID);
            String tail = " " + accessLevel + ", " + firstSeen + ", " + commandCount;
            return user == null ?  "Unknown User, ID: "+ discordID + tail : user.getName()+"#"+user.getDiscriminator() + tail;
        }

        public String getAsMention() {
            return "<@!"+this.discordID+">";
        }

        public String getDescription() {
            net.dv8tion.jda.api.entities.User user =  Utilities.getJDAInstance().getUserById(this.discordID);
            String tail = " ID: "+ discordID;
            return user == null ?  "Unknown User,"+ tail : user.getName()+"#"+user.getDiscriminator() + tail;
        }

        private PreparedStatement accessLevelStatement() {
            return Database.prepareStatement("UPDATE users SET UserLevel = ? WHERE DiscordID = ?");
        }

        private PreparedStatement incrementCommandCountStatement() {
            return Database.prepareStatement("UPDATE users SET CommandCount = CommandCount + 1 WHERE DiscordID = ?");
        }

    }