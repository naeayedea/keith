/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
public class CustomWebMvcConfig extends DelegatingWebMvcConfiguration {

    private final RequestContextArgumentResolver requestArgumentResolver;

    private final AccessControlRequestInterceptor accessControlRequestInterceptor;

    public CustomWebMvcConfig(RequestContextArgumentResolver requestArgumentResolver, AccessControlRequestInterceptor accessControlRequestInterceptor) {
        this.requestArgumentResolver = requestArgumentResolver;
        this.accessControlRequestInterceptor = accessControlRequestInterceptor;
    }

    @Override
    @NonNull
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new PackageBasedRequestMappingHandlerMapping();
    }

    @Override
    protected void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);

        argumentResolvers.add(requestArgumentResolver);
    }

    @Override
    protected void addInterceptors(@NonNull InterceptorRegistry registry) {
        super.addInterceptors(registry);

        registry.addInterceptor(accessControlRequestInterceptor);
    }
}
