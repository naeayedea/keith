package keith.managers;

import keith.commands.AccessLevel;
import keith.util.Database;
import keith.util.Utilities;
import net.dv8tion.jda.api.entities.User;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;

public class UserManager {

    private static UserManager instance;


    public static class User {
        private final String discordID;
        private AccessLevel accessLevel;
        private final String firstSeen;
        private long commandCount;

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
            return accessLevel == AccessLevel.ALL;
        }

        public void setAccessLevel(AccessLevel accessLevel) {
            if (Database.executeUpdate(accessLevelStatement(), accessLevel.num, this.discordID)) {
                this.accessLevel = accessLevel;
            }
        }

        public long getCommandCount() {
            return commandCount;
        }

        public void incrementCommandCount() {
            if (Database.executeUpdate(incrementCommandCountStatement(), this.discordID)) {
                this.commandCount++;
            }
        }

        public String toString() {
            net.dv8tion.jda.api.entities.User user =  Utilities.getJDAInstance().getUserById(this.discordID);
            String tail = " " + accessLevel + ", " + firstSeen + ", " + commandCount;
            return user == null ?  "Unknown User, ID: "+ discordID + tail : user.getName()+"#"+user.getDiscriminator() + tail;
        }

        private PreparedStatement accessLevelStatement() {
            return Database.prepareStatement("UPDATE users SET UserLevel = ? WHERE DiscordID = ?");
        }

        private PreparedStatement incrementCommandCountStatement() {
            return Database.prepareStatement("UPDATE users SET CommandCount = CommandCount + 1 WHERE DiscordID = ?");
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
