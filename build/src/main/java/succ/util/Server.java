package succ.util;

/**
 * Wrapper class for discord servers
 * Allows bot to pull database info
 * about a discord server.
 */
public class Server {

    private String serverID;
    private String firstJoined;
    private String prefix;
    private boolean banned;

    public Server(String guildID, String firstSeen, String prefix, boolean banned){
        this.serverID = guildID;
        this.firstJoined  = firstSeen;
        this.prefix = prefix;
        this.banned = banned;
    }

    public String getId(){
        return serverID;
    }

    public String getFirstJoined(){
        return firstJoined;
    }

    public String getPrefix(){
        return prefix;
    }

    public boolean isBanned(){
        return banned;
    }

    public String toString(){
        return "Server ID: "+serverID+ "First Joined: "+firstJoined+" Prefix: "+prefix;
    }

}