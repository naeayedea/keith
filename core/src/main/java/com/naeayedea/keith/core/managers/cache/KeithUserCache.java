package com.naeayedea.keith.core.managers.cache;

import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.model.user.BasicKeithUser;
import com.naeayedea.keith.common.util.Database;
import com.naeayedea.keith.common.util.KeithConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

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
    public BasicKeithUser getUser(String userId) {
        return reloadUser(userId);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME)
    public BasicKeithUser incrementCommandCount(String userId) {
        if (!database.executeUpdate(INCREMENT_COMMAND_COUNT_STATEMENT, userId)) {
            logger.error("Could not increment command count for user {} in database.", userId);
        }

        return reloadUser(userId);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#userId")
    public BasicKeithUser setAccessLevel(String userId, AccessLevel accessLevel) {
        if (!database.executeUpdate(SET_ACCESS_LEVEL_STATEMENT, accessLevel.num, userId)) {
            logger.error("Could not update access level for user {} in database.", userId);
        }

        //reload from db
        return reloadUser(userId);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME)
    public BasicKeithUser reloadUser(String userId) {
        logger.debug("Reloading user {} from database", userId);

        List<String> results = database.getStringResult(GET_CANDIDATE_STATEMENT, userId);

        if (results.size() > 1) {
            String[] result = results.get(1).split("\\t");

            logger.trace("User {} already exists", userId);

            return new BasicKeithUser(userId, AccessLevel.getLevel(result[1]), Timestamp.valueOf(result[0]).toInstant(), Long.parseLong(result[2]), Locale.of(result[3]));
        } else {
            logger.trace("User {} doesn't exist, creating.", userId);

            //user doesn't exist, need to create
            if (!database.executeUpdate(CREATE_CANDIDATE_STATEMENT, userId)) {
                logger.error("Could not create user in database.");
            }

            logger.debug("User {} created", userId);

            return new BasicKeithUser(userId, AccessLevel.USER, Instant.now(), 0, KeithConstants.DEFAULT_LOCALE);
        }
    }

    @CacheEvict(cacheNames = CACHE_NAME)
    public void clear() {
        //any side effects here if necessary
    }
}
