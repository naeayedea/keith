package keith.managers;

import keith.commands.AccessLevel;
import keith.util.Database;
import keith.util.Utilities;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserManager {


    /*
     * Internal class user
     */
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

    /*
     * UserManager code, above is internal class User
     */

    private static UserManager instance;
    private final Map<String, User> userCache;

    private UserManager(){
        userCache = new HashMap<>();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private PreparedStatement getUser() {
        return Database.prepareStatement("SELECT FirstSeen, UserLevel, CommandCount FROM users WHERE DiscordID = ?");
    }

    private PreparedStatement createUser() {
        return Database.prepareStatement("INSERT INTO users (DiscordID) VALUES (?) ");
    }

    public User getUser(String discordID) {
        User user = userCache.get(discordID);
        if(user == null) {
            //user not in cache, attempt to retrieve from database
            ArrayList<String> results = Database.getStringResult(getUser(), discordID);
            if (results.size() > 1) {
                String[] result = results.get(1).split("\\s+");
                user = new User(discordID, AccessLevel.getLevel(result[2]), result[1], Long.parseLong(result[3]));
            } else {
                //user doesn't exist, need to create
                Database.executeUpdate(createUser(), discordID);
                user =  new User(discordID, AccessLevel.USER , Instant.now().toString(), 0);
            }
            userCache.put(discordID, user);
        }
        return user;
    }

    public void clear() {
        userCache.clear();
    }

}
