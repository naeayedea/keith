package com.naeayedea.keith.managers;

import com.naeayedea.keith.commands.lib.command.AccessLevel;
import com.naeayedea.keith.model.KeithUser;
import com.naeayedea.keith.util.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Component
public class KeithUserManager {

    private static final Logger logger = LoggerFactory.getLogger(KeithUserManager.class);

    public static final String CACHE_NAME = "keith_users";

    @Value("${keith.manager.candidate.statements.getCandidate}")
    private String GET_CANDIDATE_STATEMENT;

    @Value("${keith.manager.candidate.statements.createCandidate}")
    private String CREATE_CANDIDATE_STATEMENT;

    @Value("${keith.manager.candidate.statements.setAccessLevel}")
    private String SET_ACCESS_LEVEL_STATEMENT;

    @Value("${keith.manager.candidate.statements.incrementCommandCount}")
    private String INCREMENT_COMMAND_COUNT_STATEMENT;


    private final Database database;

    public KeithUserManager(Database database) {
        this.database = database;
    }

    @NonNull
    @Cacheable(CACHE_NAME)
    public KeithUser getCandidate(String discordID) throws SQLException {
        return reloadCandidate(discordID);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME)
    public KeithUser incrementCommandCount(String discordID) throws SQLException {
        if (database.executeUpdate(INCREMENT_COMMAND_COUNT_STATEMENT, discordID)) {
            return reloadCandidate(discordID);
        }

        throw new SQLException();
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#discordID")
    public KeithUser setAccessLevel(String discordID, AccessLevel accessLevel) throws SQLException {
        KeithUser keithUser = getCandidate(discordID);

        //prevent overriding an owners access level as a protection step
        if (keithUser.getAccessLevel() != AccessLevel.OWNER) {
            if (database.executeUpdate(SET_ACCESS_LEVEL_STATEMENT, accessLevel.num, keithUser.getId())) {
                return reloadCandidate(discordID);
            }
        } else {
            logger.warn("Attempted to update owners {} permissions to {}", discordID, accessLevel);
        }

        return keithUser;
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME)
    public KeithUser reloadCandidate(String discordID) throws SQLException {
        List<String> results = database.getStringResult(GET_CANDIDATE_STATEMENT, discordID);

        if (results.size() > 1) {
            String[] result = results.get(1).split("\\s+");
            return new KeithUser(discordID, AccessLevel.getLevel(result[2]), result[1], Long.parseLong(result[3]));
        } else {
            //user doesn't exist, need to create
            if (!database.executeUpdate(CREATE_CANDIDATE_STATEMENT, discordID)) {
                throw new SQLException("Failed to create candidate " + discordID);
            }

            return new KeithUser(discordID, AccessLevel.USER, Instant.now().toString(), 0);
        }
    }

    @CacheEvict(cacheNames = CACHE_NAME)
    public void clear() {
        //any side effects here if necessary
    }

}
