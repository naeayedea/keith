package com.naeayedea.keith.core.managers.cache;

import com.naeayedea.keith.core.commands.AccessLevel;
import com.naeayedea.keith.core.model.KeithUser;
import com.naeayedea.keith.core.util.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class KeithUserCache {

    private static final Logger logger = LoggerFactory.getLogger(KeithUserCache.class);

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

    public KeithUserCache(Database database) {
        this.database = database;
    }

    @NonNull
    @Cacheable(CACHE_NAME)
    public KeithUser getUser(String discordID) {
        return reloadUser(discordID);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME)
    public KeithUser incrementCommandCount(String discordID) {
        if (!database.executeUpdate(INCREMENT_COMMAND_COUNT_STATEMENT, discordID)) {
            logger.error("Could not increment command count for user {} in database.", discordID);
        }

        return reloadUser(discordID);

    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#discordID")
    public KeithUser setAccessLevel(String discordID, AccessLevel accessLevel) {
        if (!database.executeUpdate(SET_ACCESS_LEVEL_STATEMENT, accessLevel.num, discordID)) {
            logger.error("Could not update access level for user {} in database.", discordID);
        }

        //reload from db
        return reloadUser(discordID);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME)
    public KeithUser reloadUser(String discordID) {
        logger.debug("Reloading user {} from database", discordID);

        List<String> results = database.getStringResult(GET_CANDIDATE_STATEMENT, discordID);

        if (results.size() > 1) {
            String[] result = results.get(1).split("\\s+");

            logger.trace("User {} already exists", discordID);

            return new KeithUser(discordID, AccessLevel.getLevel(result[2]), result[1], Long.parseLong(result[3]));
        } else {
            logger.trace("User {} doesn't exist, creating.", discordID);

            //user doesn't exist, need to create
            if (!database.executeUpdate(CREATE_CANDIDATE_STATEMENT, discordID)) {
                logger.error("Could not create user in database.");
            }

            logger.debug("User {} created", discordID);

            return new KeithUser(discordID, AccessLevel.USER, Instant.now().toString(), 0);
        }
    }

    @CacheEvict(cacheNames = CACHE_NAME)
    public void clear() {
        //any side effects here if necessary
    }
}
