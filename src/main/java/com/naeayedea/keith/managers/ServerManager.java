package com.naeayedea.keith.managers;

import com.naeayedea.keith.model.Server;
import com.naeayedea.keith.util.Database;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServerManager {

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

    @Value("${keith.default-prefix}")
    private String DEFAULT_PREFIX;

    public static final String CACHE_NAME = "servers";

    private final Database database;

    public ServerManager(Database database) {
        this.database = database;
    }

    @NonNull
    private Server loadServer(String serverId) {
        List<String> results = database.getStringResult(GET_SERVER_STATEMENT, serverId);

        if (results.size() > 1) {
            String[] result = results.get(1).split("\\t");

            return new Server(serverId, result[0], result[1], Boolean.parseBoolean(result[2]), result[3]);
        } else {
            //server doesn't exist yet, create
            database.executeUpdate(CREATE_SERVER_STATEMENT, serverId);

            return new Server(serverId, Instant.now().toString(), DEFAULT_PREFIX, false, null);
        }
    }

    @NonNull
    @Cacheable(value = CACHE_NAME, key = "#serverId")
    public Server getServer(String serverId) {
        return loadServer(serverId);
    }

    @NonNull
    @CacheEvict(cacheNames = CACHE_NAME, key = "#serverId")
    public Server reloadServer(String serverId) {
        return loadServer(serverId);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#serverId")
    public Server setPinChannel(String serverId, String pinChannel) throws SQLException {
        if (database.executeUpdate(SET_PIN_CHANNEL_STATEMENT, pinChannel, serverId)) {
            return reloadServer(serverId);
        } else {
            throw new SQLException("Could not retrieve server "+serverId+" from database.");
        }
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#serverId")
    public Server setBanned(String serverId, Boolean banned) throws SQLException {
        if (database.executeUpdate(SET_BANNED_STATEMENT, banned, serverId)) {
            return reloadServer(serverId);
        } else {
            throw new SQLException("Could not retrieve server "+serverId+" from database.");
        }
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#serverId")
    public Server setPrefix(String serverId, String newPrefix) throws SQLException {
        if (database.executeUpdate(SET_PREFIX_STATEMENT, newPrefix, serverId)) {
            return reloadServer(serverId);
        } else {
            throw new SQLException("Could not retrieve server "+serverId+" from database.");
        }
    }

    @CacheEvict(cacheNames = CACHE_NAME)
    public void clear() {
        //put any side effects here
    }

}
