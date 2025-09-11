/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the copyright
 * holder(s) in writing.
 */

package com.naeayedea.keith.core.api.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class CustomWebMvcConfig extends DelegatingWebMvcConfiguration {

    @Override
    @NonNull
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new PackageBasedRequestMappingHandlerMapping();
    }

}
