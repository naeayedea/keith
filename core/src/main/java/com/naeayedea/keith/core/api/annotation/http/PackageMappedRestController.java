/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.annotation.http;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * Annotation which will cause a class to be considered a RestController, the @RequestMapping annotation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
public @interface PackageMappedRestController {

    String[] packagesToExclude() default {"http", "controller"};
}

