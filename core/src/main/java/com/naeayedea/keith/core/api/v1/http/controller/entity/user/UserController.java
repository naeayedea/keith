/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.v1.http.controller.entity.user;

import com.naeayedea.keith.common.exception.KeithInternalException;
import com.naeayedea.keith.common.model.user.KeithUser;
import com.naeayedea.keith.core.api.annotation.http.PackageMappedRestController;
import com.naeayedea.keith.core.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Deliberately NOT annotated with {@code @KeithHttpCommandController}/{@code @RequireAccessLevel} -
 * resolving/creating a user is the onboarding step every other authenticated endpoint depends on,
 * so it can't itself require the user to already exist.
 *
 * <p>Resolution today is by {@code platformUserId} alone - see the note on
 * {@code KeithCommandContext} for why {@code platform} is still carried on the wire.
 */
@PackageMappedRestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<KeithUser> getOrCreateUser(@RequestParam String platform, @RequestParam String platformUserId) throws KeithInternalException {
        return ResponseEntity.ok(userService.getOrCreateUser(platformUserId));
    }
}
