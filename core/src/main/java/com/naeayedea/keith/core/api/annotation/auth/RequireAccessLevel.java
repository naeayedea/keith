/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.annotation.auth;

import com.naeayedea.keith.common.model.user.AccessLevel;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAccessLevel {

    AccessLevel baseAccessLevel();
}
