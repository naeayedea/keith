package keith.managers;

import keith.util.Database;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;

public class UserManager {

    private static UserManager instance;

    public enum AccessLevel {
        BANNED (0),
        USER (1),
        ADMIN (2),
        OWNER (3);

        AccessLevel(int num){}

        public static AccessLevel getLevel(String num){
            switch (num) {
                case "0":
                    return BANNED;
                case "2":
                    return ADMIN;
                case "3":
                    return OWNER;
                default:
                    return USER;
            }
        }

    }

    public static class User {
        private final String discordID;
        private AccessLevel accessLevel;
        private final String firstSeen;
        private final long commandCount;

        public User(String discordID, AccessLevel accessLevel, String firstSeen, long commandCount) {
            this.discordID = discordID;
            this.accessLevel = accessLevel;
            this.firstSeen = firstSeen;
            this.commandCount = commandCount;
        }

        public String getFirstSeen() {
            return firstSeen;
        }

        public String getDiscordID() {
            return discordID;
        }

        public AccessLevel getAccessLevel() {
            return accessLevel;
        }

        public boolean isBanned() {
            return accessLevel == UserManager.AccessLevel.BANNED;
        }

        public void setAccessLevel(AccessLevel accessLevel) {
            this.accessLevel = accessLevel;
        }


        public long getCommandCount() {
            return commandCount;
        }

        public String toString() {
            return discordID + " " + accessLevel + ", " + firstSeen + ", " + commandCount;
        }
    }

    private UserManager(){

    }

    private PreparedStatement getUser() {
        return Database.prepareStatement("SELECT FirstSeen, UserLevel, CommandCount FROM users WHERE DiscordID = ?");
    }

    private PreparedStatement createUser() {
        return Database.prepareStatement("INSERT INTO users (DiscordID) VALUES (?) ");
    }

    public User getUser(String discordID) {
        ArrayList<String> results = Database.getStringResult(getUser(), discordID);
        if (results.size() > 1) {
            String[] result = results.get(1).split("\\s+");
            return new User(discordID, AccessLevel.getLevel(result[1]), result[2], Long.parseLong(result[3]));
        } else {
            //user doesn't exist, need to create
            Database.executeUpdate(createUser(), discordID);
            return new User(discordID, AccessLevel.USER , Instant.now().toString(), 0);
        }
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

}
