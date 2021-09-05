package keith.managers;

import keith.commands.AccessLevel;
import keith.util.Database;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {

    /*
     * Internal class user
     */

    public static class Server {

        private final String serverID;
        private final String firstSeen;
        private String prefix;
        private Boolean banned;
        private String pin_channel;

        public Server(String serverID, String firstSeen, String prefix, Boolean banned, String pin_channel) {

            this.serverID = serverID;
            this.firstSeen = firstSeen;
            this.prefix = prefix;
            this.banned = banned;
            this.pin_channel = pin_channel;
        }


        public String getPin_channel() {
            return pin_channel;
        }

        public void setPin_channel(String pin_channel) {
            this.pin_channel = pin_channel;
        }

        public Boolean isBanned() {
            return banned;
        }

        public void setBanned(Boolean banned) {
            if (Database.executeUpdate(setBannedStatement(), banned, this.serverID)) {
                this.banned = banned;
            }
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String newPrefix) {
            if(Database.executeUpdate(prefixStatement(), newPrefix, this.serverID)) {
                prefix = newPrefix;
            }
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
    }

    /*
     * ServerManager code, above is internal class server
     */

    private static ServerManager instance;
    private Map<String, Server> serverCache;

    private ServerManager() {
        serverCache = new HashMap<>();
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    private PreparedStatement addServer() {
        return Database.prepareStatement("INSERT INTO servers (ServerID) VALUES (?)");
    }

    //String serverID, String firstSeen, String prefix, Boolean banned, String pin_channel
    private PreparedStatement getServer() {
        return Database.prepareStatement("SELECT FirstSeen, prefix, banned, pin_channel FROM servers where serverID = ?");
    }

    public Server getServer(String guildID) {
        Server server = serverCache.get(guildID);
        if (server == null ) {
            //server not in cache, attempt to retrieve from database
            ArrayList<String> results = Database.getStringResult(getServer(), guildID);
            if (results.size() > 1) {
                String[] result = results.get(1).split("\\t");
                server = new Server(guildID, result[0], result[1], Boolean.parseBoolean(result[2]), result[3]);
            } else {
                //server doesn't exist yet, create
                Database.executeUpdate(addServer(), guildID);
                server = new Server(guildID, Instant.now().toString(), "?", false, null);
            }
            serverCache.put(guildID, server);
        }
        return server;
    }

    public void clear() {
        serverCache.clear();
    }

}
