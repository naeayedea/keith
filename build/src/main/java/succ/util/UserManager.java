package succ.util;

import succ.util.logs.ConsoleLogger;
import java.util.ArrayList;

/**
 * Allows bot to manage and interact with users as well as accessing relevant information
 * from the database.
 */
public class UserManager {

    public static final int USERLEVEL_BANNED = 0;
    public static final int USERLEVEL_GENERAL = 1;
    public static final int USERLEVEL_ADMIN = 2;
    User currentUser;
    Database database;
    ConsoleLogger log;
    public UserManager(Database database){
        this.database=database;
        log = new ConsoleLogger();
    }

    /**
     * returns a user object from the database
     * @param   discordId   the users id.
     * @returns a user object
     */
    public User getUser(String discordid){
        if(retrieveUser(discordid))
        return currentUser;
        return null;
    }

    /*
     * retrieves the users info from the database.
     * returns true if successful.
     */
    private boolean retrieveUser(String discordid){
        ArrayList<String> rs = database.select("* FROM users WHERE DiscordID = "+discordid);
        try{ ;
        String firstSeen = rs.get(1);
        int accessLevel = Integer.parseInt(rs.get(2).toString());
        currentUser = new User(discordid, firstSeen, accessLevel);
        return true;
        }
        catch (IndexOutOfBoundsException e){
            log.printWarning("user query returning insufficient columns - database issue?");
        }
        return false;
    }

    /**
     * Verifies if a user exists within the database or not
     * @param discordid the users discord id
     * @return true if exist
     */
    public boolean inDatabase(String discordid){
        if(database.exists("DiscordID="+discordid, "users")){
            return true;
        }
        return false;
    }

    /**
     * Creates a database entry for a new bot user
     * @param discordid users discord id
     * @return true if successful
     */
    public boolean createUser(String discordid){
        log.printSuccess("Inserting user into database");
        return database.insert("(DiscordID, UserLevel) VALUES ("+discordid+", 1)", "users");
    }

    /**
     * Updates users access level within the database
     * warning: providing admin access can be dangerous
     * @param discordid users discord id
     * @param newLevel USERLEVEL_BANNED, GENERAL or ADMIN
     * @return true if successful.
     */
    public boolean updateAccessLevel(String discordid, int newLevel){
        return database.update("UserLevel = "+newLevel+" WHERE DiscordID ="+discordid, "users");
    }

}
