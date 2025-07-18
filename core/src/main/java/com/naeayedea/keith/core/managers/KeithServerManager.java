package com.naeayedea.keith.core.managers;

import com.naeayedea.keith.common.model.server.BasicKeithServer;
import com.naeayedea.keith.common.util.Database;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Component
public class KeithServerManager {

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

    public KeithServerManager(Database database) {
        this.database = database;
    }

    @NonNull
    private BasicKeithServer loadServer(String serverId) {
        List<String> results = database.getStringResult(GET_SERVER_STATEMENT, serverId);

        if (results.size() > 1) {
            String[] result = results.get(1).split("\\t");

            return new BasicKeithServer(serverId, Timestamp.valueOf(result[0]).toInstant(), result[1], Boolean.parseBoolean(result[2]), result[3]);
        } else {
            //server doesn't exist yet, create
            database.executeUpdate(CREATE_SERVER_STATEMENT, serverId);

            return new BasicKeithServer(serverId, Instant.now(), DEFAULT_PREFIX, false, null);
        }
    }

    @NonNull
    @Cacheable(value = CACHE_NAME, key = "#serverId")
    public BasicKeithServer getServer(String serverId) {
        return loadServer(serverId);
    }

    @NonNull
    @CacheEvict(cacheNames = CACHE_NAME, key = "#serverId")
    public BasicKeithServer reloadServer(String serverId) {
        return loadServer(serverId);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#serverId")
    public BasicKeithServer setPinChannel(String serverId, String pinChannel) throws SQLException {
        if (database.executeUpdate(SET_PIN_CHANNEL_STATEMENT, pinChannel, serverId)) {
            return reloadServer(serverId);
        } else {
            throw new SQLException("Could not retrieve server "+serverId+" from database.");
        }
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#serverId")
    public BasicKeithServer setBanned(String serverId, Boolean banned) throws SQLException {
        if (database.executeUpdate(SET_BANNED_STATEMENT, banned, serverId)) {
            return reloadServer(serverId);
        } else {
            throw new SQLException("Could not retrieve server "+serverId+" from database.");
        }
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#serverId")
    public BasicKeithServer setPrefix(String serverId, String newPrefix) throws SQLException {
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
