package com.naeayedea.model;

import com.naeayedea.keith.util.Database;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.PreparedStatement;

public class Server {

        private final String serverID;
        
        private final String firstSeen;
        
        private String prefix;
        
        private Boolean banned;
        
        private String pinChannel;

        public Server(String serverID, String firstSeen, String prefix, Boolean banned, String pinChannel) {

            this.serverID = serverID;
            this.firstSeen = firstSeen;
            this.prefix = prefix;
            this.banned = banned;
            this.pinChannel = pinChannel;
        }

        public String getPinChannel() {
            return pinChannel;
        }

        public void setPinChannel(String pinChannel) {
            if (Database.executeUpdate(pinChannelStatement(), pinChannel, this.serverID)) {
                this.pinChannel = pinChannel;
            }
        }

        public Boolean isBanned() {
            return banned;
        }

        public boolean setBanned(Boolean banned) {
            if (Database.executeUpdate(setBannedStatement(), banned, this.serverID)) {
                this.banned = banned;
                return true;
            }
            return false;
        }

        public String getPrefix() {
            return prefix;
        }

        public boolean setPrefix(String newPrefix) {
            if(Database.executeUpdate(prefixStatement(), newPrefix, this.serverID)) {
                prefix = newPrefix;
                return true;
            }
            return false;
        }

        private PreparedStatement pinChannelStatement() {
            return Database.prepareStatement("UPDATE servers SET PinChannel = ? WHERE ServerID = ?");
        }

        private PreparedStatement prefixStatement() {
            return Database.prepareStatement("UPDATE servers SET prefix = ? WHERE ServerID = ?");
        }

        private PreparedStatement setBannedStatement() {
            return Database.prepareStatement("UPDATE servers SET banned = ? WHERE ServerID = ?");
        }

        public String getFirstSeen() {
            return firstSeen;
        }

        public String getServerID() {
            return serverID;
        }

        public String toString() {
            Guild guild =  Utilities.getJDAInstance().getGuildById(this.serverID);
            String tail = "> First Seen: " + firstSeen + ", Prefix: " + prefix +", Pin Channel: " + pinChannel;
            return guild == null ?  "Unknown Server, ID: "+ serverID + tail : guild.getName()+"<"+guild.getId() + tail;
        }
    }