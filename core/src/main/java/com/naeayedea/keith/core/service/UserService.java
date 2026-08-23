/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.service;

import com.naeayedea.keith.common.exception.KeithException;
import com.naeayedea.keith.common.exception.KeithInternalException;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.model.user.KeithUser;
import com.naeayedea.keith.core.managers.KeithUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final KeithUserManager userManager;

    public UserService(KeithUserManager userManager) {
        this.userManager = userManager;
    }

    public KeithUser getOrCreateUser(String userId) throws KeithInternalException{
        try {
            return userManager.getOrCreateUser(userId);
        } catch (Throwable t) {
            throw new KeithInternalException("Could not create or retrieve user with id " + userId, t);
        }
    }

    public KeithUser getUser(String userId) {
        try {
            return userManager.getUser(userId);
        } catch (Throwable throwable) {
            logger.warn("Attempted to retrieve user {} which does not exist", userId);
            return null;
        }
    }

    public KeithUser setAccessLevel(String userId, AccessLevel accessLevel) throws KeithException {
        KeithUser keithUser = userManager.getUser(userId);

        //prevent overriding an owners access level as a protection step
        if (keithUser.getAccessLevel() != AccessLevel.OWNER) {
            //reload from db
            return userManager.setAccessLevel(userId, accessLevel);
        } else {
            logger.warn("Attempted to update owner {}'s permissions to {}", userId, accessLevel);

            throw new KeithException("Attempted to update owner ({}) permissions to {}", userId, accessLevel);
        }
    }


}
