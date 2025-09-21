/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.config;

import com.naeayedea.keith.core.api.v1.exception.KeithUserFacingException;
import com.naeayedea.keith.core.api.v1.http.internal.model.request.KeithCommandContext;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RequestContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return KeithCommandContext.class.equals(parameter.getParameterType());
    }

    @Override
    public KeithCommandContext resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            return null;
        }

        String userId = request.getParameter("userId");

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User Id parameter is missing");
        }

        KeithCommandContext commandContext = KeithCommandContext.builder()
            .userId(userId)
            .build();

        try {
            return commandContext;
        } catch (IllegalStateException e) {
            throw new KeithUserFacingException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Throwable t) {
            throw new Exception(t);
        }

    }

}
