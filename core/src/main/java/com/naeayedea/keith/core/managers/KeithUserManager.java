/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.managers;

import com.naeayedea.keith.common.exception.KeithInternalException;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.model.user.BasicKeithUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class KeithUserManager {

    private static final Logger logger = LoggerFactory.getLogger(KeithUserManager.class);

    public static final String CACHE_NAME = "keith_users";

    private final String GET_USER_STATEMENT = "SELECT user_id, access_level, command_count, first_seen, 'ENGLISH' as 'locale' FROM user WHERE user_id = :user_id" ;
    private final String CREATE_USER_STATEMENT = "INSERT INTO user (user_id) VALUES (:user_id)";
    private final String SET_ACCESS_LEVEL_STATEMENT = "UPDATE user SET access_level = :access_level WHERE user_id = :user_id";
    private final String INCREMENT_COMMAND_COUNT_STATEMENT = "UPDATE user SET command_count = command_count + 1 WHERE user_id = :user_id";

    private final JdbcClient jdbcClient;

    public KeithUserManager(JdbcClient client) {
        this.jdbcClient = client;
    }

    @NonNull
    @Cacheable(CACHE_NAME)
    public BasicKeithUser getOrCreateUser(@NonNull String userId) throws KeithInternalException {
        return loadUserFromPersistenceLayer(userId, true);
    }

    @NonNull
    @Cacheable(CACHE_NAME)
    public BasicKeithUser getUser(@NonNull String userId) throws KeithInternalException {
        return loadUserFromPersistenceLayer(userId, false);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME)
    @Transactional(rollbackFor = KeithInternalException.class)
    public BasicKeithUser incrementCommandCount(@NonNull String userId) throws KeithInternalException {
        int numRowsAffected = jdbcClient.sql(INCREMENT_COMMAND_COUNT_STATEMENT)
            .param("user_id", userId)
            .update();

        if (numRowsAffected > 1) {
            logger.error("Updated more than one users command count for query: {}, rows affected: {}. Rolling back.", INCREMENT_COMMAND_COUNT_STATEMENT, numRowsAffected);

            throw new KeithInternalException("INCREMENT_COMMAND_COUNT_STATEMENT updated more than one row. Critical error avoided.");
        } else if (numRowsAffected < 1) {
            logger.error("Could not increment command count for user {} in database.", userId);
        }

        return loadUserFromPersistenceLayer(userId, false);
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#userId")
    public BasicKeithUser setAccessLevel(@NonNull String userId, AccessLevel accessLevel) throws KeithInternalException {
        int numRowsAffected = jdbcClient.sql(SET_ACCESS_LEVEL_STATEMENT)
            .param("user_id", userId)
            .param("access_level", accessLevel)
            .update();

        if (numRowsAffected > 1) {
            logger.error("Attempted to update more than one users access level with query: {}, rows affected: {}. Rolling back.", SET_ACCESS_LEVEL_STATEMENT, numRowsAffected);

            throw new KeithInternalException("SET_ACCESS_LEVEL_STATEMENT updated more than one row. Critical error avoided.");
        } else if (numRowsAffected < 1) {
            logger.error("Could not update access level for user {} in database.", userId);
            throw new KeithInternalException("Could not update access level for user {} to {}", userId, accessLevel);
        }

        return loadUserFromPersistenceLayer(userId, false);
    }

    @NonNull
    private BasicKeithUser loadUserFromPersistenceLayer(@NonNull String userId, boolean createIfMissing) throws KeithInternalException {
        logger.debug("Reloading user {} from database", userId);

        List<Map<String, Object>> rows = jdbcClient.sql(GET_USER_STATEMENT)
            .param("user_id", userId)
            .query()
            .listOfRows();

        Map<String, Object> result = rows.isEmpty() ? Map.of() : rows.getFirst();

        if (result.size() >= 4) {
            logger.trace("User {} already exists", userId);

            if (!userId.equals(result.get("user_id"))) {
                throw new KeithInternalException("Retrieved unintended user from database. Wanted: "+ userId + ", got: " + result.get("user_id"));
            }

            return new BasicKeithUser(userId, AccessLevel.getLevel(result.get("access_level").toString()), Timestamp.valueOf(result.get("first_seen").toString()).toInstant(), Long.parseLong(result.get("command_count").toString()), Locale.of(result.get("locale").toString()));
        } else if (createIfMissing) {
            logger.trace("User {} doesn't exist, creating.", userId);

            int numRowsAffected = jdbcClient.sql(CREATE_USER_STATEMENT)
                .param("user_id", userId)
                .update();

            if (numRowsAffected > 1) {
                logger.error("Updated more than one user when creating new user {}: {}, rows affected: {}. Rolling back.", userId, CREATE_USER_STATEMENT, numRowsAffected);

                throw new KeithInternalException("CREATE_USER_STATEMENT updated more than one row. Critical error avoided.");
            } else if (numRowsAffected < 1) {
                logger.error("Could create user {} in database.", userId);
                throw new KeithInternalException("Could not create user " + userId);
            }

            logger.debug("User {} created", userId);

            //load the newly created user
            return loadUserFromPersistenceLayer(userId, false);
        }

        throw new KeithInternalException("Could not load user from database.");
    }

    @CacheEvict(cacheNames = CACHE_NAME)
    public void clear() {
        //any side effects here if necessary
    }

}
