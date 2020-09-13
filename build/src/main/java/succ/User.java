package succ;

/**
 * Allows storage of information and interaction with users.
 */
public class User {

    int discordid;
    int accessLevel;
    String firstSeen;
    Database database;

    public User(int discordid, Database database){
        this.discordid=discordid;
        getUserInfo();
        this.database=database;
    }

    /**
     * retrieves the users info from the database.
     */
    private void getUserInfo(){

    }

    /**
     * Updates users accesslevel via a command
     * @param newLevel  the users new access level
     * @return a string containing a success or error message for bot response.
     */
    public String updateAccessLevel(int newLevel){
        return null;
    }

    /**
     * @return users bot access level
     */
    public int getAccessLevel(){
        return accessLevel;
    }

    /**
     * @return date user was first seen by bot
     */
    public String getFirstSeen(){
        return firstSeen;
    }

    public String toString(){
        return "bitch";
    }

    public boolean inDatabase(){
        if(database.exists(discordid+"= DiscordID", "users")){
            return true;
        }
        return false;
    }

}
