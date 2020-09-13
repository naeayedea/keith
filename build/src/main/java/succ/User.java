package succ;

/**
 * Allows storage of information and interaction with users.
 */
public class User {

    String discordid;
    int accessLevel;
    String firstSeen;
    Database database;

    public User(String discordid, String firstSeen, int accessLevel){
        this.discordid=discordid;
        this.firstSeen = firstSeen;
        this.accessLevel = accessLevel;
        this.database=database;
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
        switch(accessLevel){
            case 0:
                return "Discord ID: "+discordid+" First Seen: "+firstSeen+" Access Level: BANNED";
            case 1:
                return "Discord ID: "+discordid+" First Seen: "+firstSeen+" Access Level: GENERAL";
            case 2:
                return "Discord ID: "+discordid+" First Seen: "+firstSeen+" Access Level: ADMIN";
        }
        return "something has went wrong!";
    }


}
