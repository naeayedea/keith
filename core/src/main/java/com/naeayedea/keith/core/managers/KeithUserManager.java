package com.naeayedea.keith.core.managers;

import com.naeayedea.keith.core.commands.AccessLevel;
import com.naeayedea.keith.core.managers.cache.KeithUserCache;
import com.naeayedea.keith.core.model.KeithUser;
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
    public KeithUser getUser(String discordID) {
        return userCache.getUser(discordID);
    }

    @NonNull
    public void incrementCommandCount(String discordID) {
        userCache.incrementCommandCount(discordID);
    }

    @NonNull
    public KeithUser setAccessLevel(String discordID, AccessLevel accessLevel) {
        KeithUser keithUser = getUser(discordID);

        //prevent overriding an owners access level as a protection step
        if (keithUser.getAccessLevel() != AccessLevel.OWNER) {
            return userCache.setAccessLevel(discordID, accessLevel);
        } else {
            logger.warn("Attempted to update owners {} permissions to {}", discordID, accessLevel);
        }

        return keithUser;
    }

    @NonNull
    public KeithUser reloadUser(String discordID){
        return userCache.reloadUser(discordID);
    }

    public void clear() {
        userCache.clear();
    }

}
