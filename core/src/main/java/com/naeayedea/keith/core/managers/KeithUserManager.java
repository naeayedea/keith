package com.naeayedea.keith.core.managers;

import com.naeayedea.keith.core.commands.lib.AccessLevel;
import com.naeayedea.keith.core.managers.cache.KeithUserCache;
import com.naeayedea.keith.core.model.user.BasicKeithUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class KeithUserManager {

    private static final Logger logger = LoggerFactory.getLogger(KeithUserManager.class);

    private final KeithUserCache userCache;

    public KeithUserManager(KeithUserCache userCache) {
        this.userCache = userCache;
    }

    @NonNull
    public BasicKeithUser getUser(String userId) {
        return userCache.getUser(userId);
    }

    @NonNull
    public void incrementCommandCount(String userId) {
        userCache.incrementCommandCount(userId);
    }

    @NonNull
    public BasicKeithUser setAccessLevel(String userId, AccessLevel accessLevel) {
        BasicKeithUser keithUser = getUser(userId);

        //prevent overriding an owners access level as a protection step
        if (keithUser.getAccessLevel() != AccessLevel.OWNER) {
            return userCache.setAccessLevel(userId, accessLevel);
        } else {
            logger.warn("Attempted to update owners {} permissions to {}", userId, accessLevel);
        }

        return keithUser;
    }

    @NonNull
    public BasicKeithUser reloadUser(String userId){
        return userCache.reloadUser(userId);
    }

    public void clear() {
        userCache.clear();
    }

}
