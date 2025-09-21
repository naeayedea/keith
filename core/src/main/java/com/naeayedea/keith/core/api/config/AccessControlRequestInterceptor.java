/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.config;

import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.model.user.KeithUser;
import com.naeayedea.keith.core.api.annotation.auth.RequireAccessLevel;
import com.naeayedea.keith.core.api.annotation.http.KeithHttpCommandController;
import com.naeayedea.keith.core.api.v1.exception.KeithUserFacingException;
import com.naeayedea.keith.core.service.UserService;
import com.naeayedea.keith.core.util.AnnotationUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;
import java.util.List;

@Component
public class AccessControlRequestInterceptor implements HandlerInterceptor {

    private final UserService userService;

    private static final List<Class<? extends Annotation>> supportedAnnotations = List.of(KeithHttpCommandController.class, RequireAccessLevel.class);

    public AccessControlRequestInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        AccessLevel requiredAccessLevel = getRequiredAccessLevel(method);

        if (requiredAccessLevel == null) {
            return true;
        }

        String userId = request.getParameter("userId");

        if (userId == null) {
            throw new KeithUserFacingException(HttpStatus.BAD_REQUEST, "Missing required parameter 'userId'");
        }

        KeithUser user = userService.getUser(userId);

        if (user == null) {
            throw new KeithUserFacingException(HttpStatus.UNAUTHORIZED, "User in command does not exist");
        }

        if (!requiredAccessLevel.isSameOrLowerPermissionLevel(user.getAccessLevel())) {
            throw new KeithUserFacingException(HttpStatus.FORBIDDEN, "User's access level does not permit this action");
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private AccessLevel getRequiredAccessLevel(HandlerMethod method) {
        AccessLevel requiredAccessLevel = null;

        if (method.getMethod().isAnnotationPresent(RequireAccessLevel.class)) {
            RequireAccessLevel locatedAnnotation = method.getMethod().getAnnotation(RequireAccessLevel.class);

            if (locatedAnnotation != null) {
                requiredAccessLevel = locatedAnnotation.baseAccessLevel();
            }
        }

        for (Class<? extends Annotation> supportedAnnotation : supportedAnnotations) {
            if (requiredAccessLevel != null) {
                break;
            }

            Annotation locatedAnnotation = AnnotationUtilities.findAnnotationOnClass(method.getMethod().getDeclaringClass(), supportedAnnotation);

            switch (locatedAnnotation) {
                case KeithHttpCommandController keithHttpCommandController ->
                    requiredAccessLevel = keithHttpCommandController.baseAccessLevel();
                case RequireAccessLevel requiresAccessLevel ->
                    requiredAccessLevel = requiresAccessLevel.baseAccessLevel();
                case null, default -> {}
            }
        }

        return requiredAccessLevel;
    }
}
