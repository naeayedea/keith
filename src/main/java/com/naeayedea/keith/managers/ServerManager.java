package com.naeayedea.keith.managers;

import com.naeayedea.keith.model.Server;
import com.naeayedea.keith.util.Database;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServerManager {

    private final Map<String, Server> serverCache;

    @Value("${keith.manager.server.statements.getServer}")
    private String GET_SERVER_STATEMENT;

    @Value("${keith.manager.server.statements.createServer}")
    private String CREATE_SERVER_STATEMENT;

    @Value("${keith.manager.server.statements.setPinChannel}")
    private String SET_PIN_CHANNEL_STATEMENT;

    @Value("${keith.manager.server.statements.setBanned}")
    private String SET_BANNED_STATEMENT;

    @Value("${keith.manager.server.statements.setPrefix}")
    private String SET_PREFIX_STATEMENT;

    @Value("${keith.defaultPrefix}")
    private String DEFAULT_PREFIX;

    private final Database database;

    public ServerManager(Database database) {
        this.database = database;
        serverCache = new HashMap<>();
    }

    public Server getServer(String guildID) {
        Server server = serverCache.get(guildID);

        if (server == null) {
            //server not in cache, attempt to retrieve from database

            server = reloadServer(guildID);

            serverCache.put(guildID, server);
        }
        return server;
    }

    private Server reloadServer(String guildID) {
        List<String> results = database.getStringResult(GET_SERVER_STATEMENT, guildID);

        if (results.size() > 1) {
            String[] result = results.get(1).split("\\t");

            return new Server(guildID, result[0], result[1], Boolean.parseBoolean(result[2]), result[3]);
        } else {
            //server doesn't exist yet, create
            database.executeUpdate(CREATE_SERVER_STATEMENT, guildID);

            return new Server(guildID, Instant.now().toString(), DEFAULT_PREFIX, false, null);
        }
    }

    public Server setPinChannel(String serverID, String pinChannel) throws SQLException {
        if (database.executeUpdate(SET_PIN_CHANNEL_STATEMENT, pinChannel, serverID)) {
            return reloadServer(serverID);
        } else {
            throw new SQLException("Could not retrieve server "+serverID+" from database.");
        }
    }

    public Server setBanned(String serverID, Boolean banned) throws SQLException {
        if (database.executeUpdate(SET_BANNED_STATEMENT, banned, serverID)) {
            return reloadServer(serverID);
        } else {
            throw new SQLException("Could not retrieve server "+serverID+" from database.");
        }
    }

    public Server setPrefix(String serverID, String newPrefix) throws SQLException {
        if (database.executeUpdate(SET_PREFIX_STATEMENT, newPrefix, serverID)) {
            return reloadServer(serverID);
        } else {
            throw new SQLException("Could not retrieve server "+serverID+" from database.");
        }
    }


    public void clear() {
        serverCache.clear();
    }

}
