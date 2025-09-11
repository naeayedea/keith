/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the copyright
 * holder(s) in writing.
 */

package com.naeayedea.keith.core.api.config;

import com.naeayedea.keith.core.api.annotation.PackageMappedRestController;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Converts controller + method signatures into valid http request mappings, will expand package structures into
 * HTTP paths e.g. api.v1.http.command -> /api/v1/command (http excluded explicitly). Will also add in equivalent
 * trailing slash/no trailing slash endpoints for every endpoint it processes.
 */
public class PackageBasedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {


    @Override
    protected RequestMappingInfo getMappingForMethod(@NonNull Method method, @NonNull Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

        //if info is null, indicates method does not have a <METHOD>Mapping annotation so we should ignore
        if (info == null) {
            return null;
        }

        //get a set of valid paths from the method, typically we build this into a trailing slash vs no trailing slash path
        Set<String> paths = buildPathsWithTrailingAndNonTrailingSlashes(info);

        //rebuild with the new paths
        info = info.mutate().paths(paths.toArray(new String[0])).build();

        //if the class doesn't have a package mapped controller ignore, or if the mapping is overridden with an explicit @RequestMapping annotation
        if (!handlerType.isAnnotationPresent(PackageMappedRestController.class) || handlerType.isAnnotationPresent(RequestMapping.class)) {
            return info;
        }

        //get the base path from the package structure
        String fullBasePath = getFullBasePathFromPackage(handlerType);

        //combine
        RequestMappingInfo prefixInfo = RequestMappingInfo
            //add both the slash and trailing slash version
            .paths(fullBasePath)
            .build();

        return prefixInfo.combine(info);
    }

    /**
     * For every current path in the request mapping, create a version with and without a trailing slash
     *
     * @param info the {@link RequestMappingInfo} object with the current state of the mapping
     * @return a {@code Set<String>} containing the original paths + an equivalent with/without a trailing slash
     */
    private static Set<String> buildPathsWithTrailingAndNonTrailingSlashes(RequestMappingInfo info) {
        Set<String> paths = new HashSet<>();

        for (String path : info.getPatternValues()) {
            String pathWithSlash;
            String pathWithoutSlash;
            if (path.endsWith("/")) {
                pathWithSlash = path;
                pathWithoutSlash = path.substring(0, path.length() - 1);
            } else {
                pathWithSlash = path + "/";
                pathWithoutSlash = path;
            }

            paths.add(pathWithSlash);
            paths.add(pathWithoutSlash);
        }

        return paths;
    }

    /**
     * Retrieve a valid HTTP path based on the base path of the method. Will trim "http" from the path. For example,
     * if the package is any.amount.of.packages.<b>api</b>.v1.command.general.GeneralCommandController, the HTTP base path for that
     * controller would automatically be set to /api/v1/command/general/
     *
     * @param handlerType the class of the controller being updated
     * @return a String path built from the package of the class.
     */
    private static String getFullBasePathFromPackage(Class<?> handlerType) {
        String packageName = handlerType.getPackage().getName();

        String stringToFind = "api";
        int idx = packageName.lastIndexOf(stringToFind);

        String basePath = "";
        if (idx != -1) {
            basePath = packageName.substring(idx + stringToFind.length()).replaceAll("\\.", "/");
        }

        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }

        if (!basePath.endsWith("/") && !basePath.isBlank()) {
            basePath += "/";
        }

        if (basePath.contains("/http")) {
            basePath = basePath.replaceAll("/http", "");
        }

        return "/api/" + basePath;
    }
}
