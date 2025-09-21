/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.annotation.http;

import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.core.api.annotation.auth.RequireAccessLevel;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PackageMappedRestController
@RequireAccessLevel(baseAccessLevel = AccessLevel.OWNER)
public @interface KeithHttpCommandController {

    @AliasFor(annotation = RequireAccessLevel.class, attribute = "baseAccessLevel")
    AccessLevel baseAccessLevel();
}
