package com.naeayedea.keith.managers;

import com.naeayedea.keith.util.Database;
import com.naeayedea.model.Server;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {

    private final Map<String, Server> serverCache;

    public ServerManager() {
        serverCache = new HashMap<>();
    }

    private PreparedStatement addServer() {
        return Database.prepareStatement("INSERT INTO servers (ServerID) VALUES (?)");
    }

    //String serverID, String firstSeen, String prefix, Boolean banned, String pin_channel
    private PreparedStatement getServer() {
        return Database.prepareStatement("SELECT FirstSeen, prefix, banned, PinChannel FROM servers where serverID = ?");
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
