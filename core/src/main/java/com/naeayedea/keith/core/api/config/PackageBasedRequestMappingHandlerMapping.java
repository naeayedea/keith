package com.naeayedea.keith.core.api.config;

import com.naeayedea.keith.core.api.annotation.http.PackageMappedRestController;
import com.naeayedea.keith.core.util.AnnotationUtilities;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts controller + method signatures into valid http request mappings, will expand package structures into
 * HTTP paths e.g. api.v1.http.command -> /api/v1/command (http excluded explicitly). Will also add in equivalent
 * trailing slash/no trailing slash endpoints for every endpoint it processes.
 */
public class PackageBasedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(PackageBasedRequestMappingHandlerMapping.class);

    @Override
    protected RequestMappingInfo getMappingForMethod(
        @NonNull Method method,
        @NonNull Class<?> handlerType
    ) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

        //if info is null, indicates method does not have a <METHOD>Mapping annotation so we should ignore
        if (info == null) {
            return null;
        }

        //get a set of valid paths from the method, typically we build this into a trailing slash vs no trailing slash path
        Set<String> paths = buildPathsWithTrailingAndNonTrailingSlashes(info.getPatternValues());

        //rebuild with the new paths
        info = info.mutate().paths(paths.toArray(new String[0])).build();

        //if the class doesn't have a package mapped controller ignore, or if the mapping is overridden with an explicit @RequestMapping annotation
        if (handlerType.isAnnotationPresent(RequestMapping.class) || !isClassValid(handlerType)) {
            return info;
        }

        PackageMappedRestController annotationInstance = AnnotationUtilities.findAnnotationOnClass(handlerType, PackageMappedRestController.class);

        List<String> packagesToExclude = new ArrayList<>();

        if (annotationInstance != null) {
            packagesToExclude = Arrays.asList(annotationInstance.packagesToExclude());
        }

        //get the base path from the package structure
        String fullBasePath = getFullBasePathFromPackage(handlerType, packagesToExclude);

        paths = buildPathsWithTrailingAndNonTrailingSlashes(List.of(fullBasePath));

        //combine
        RequestMappingInfo prefixInfo = RequestMappingInfo
            //add both the slash and trailing slash version
            .paths(paths.toArray(new String[0]))
            .build();

        info = prefixInfo.combine(info);

        Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
        String methodLabel = methods.isEmpty() ? "ANY" : methods.toString();

        for (String path : info.getDirectPaths()) {
            logger.debug("Dynamically registering controller endpoint for {}: {} {}",
                handlerType.getSimpleName(),
                methodLabel,
                (Stream.of(fullBasePath, path).filter(string -> !string.isBlank()).collect(Collectors.joining("/"))).replaceAll("/{2,}", "/"));
        }

        return info;
    }

    private static boolean isClassValid(@NonNull Class<?> clazz) {
        return AnnotationUtilities.findAnnotationOnClass(clazz, PackageMappedRestController.class) != null;
    }

    /**
     * For every current path in the request mapping, create a version with and without a trailing slash
     *
     * @param inputPaths the paths to build from
     * @return a {@code Set<String>} containing the original paths + an equivalent with/without a trailing slash
     */
    @NonNull
    private static Set<String> buildPathsWithTrailingAndNonTrailingSlashes(@NonNull Collection<String> inputPaths) {
        Set<String> paths = new HashSet<>();

        for (String path : inputPaths) {
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
     * Retrieve a valid HTTP path based on the base path of the method. Will trim everything up to and
     * including the "api" package segment. For example, if the package is
     * any.amount.of.packages.<b>api</b>.v1.command.general.GeneralCommandController, the HTTP base path for
     * that controller would automatically be set to /api/v1/command/general/
     *
     * @param handlerType       the class of the controller being updated
     * @param packagesToExclude package segments that should be stripped out of the resulting path
     * @return a String path built from the package of the class.
     */
    @NonNull
    private static String getFullBasePathFromPackage(
        @NonNull Class<?> handlerType,
        @NonNull List<String> packagesToExclude
    ) {
        Package pkg = handlerType.getPackage();
        String packageName = pkg == null ? "" : pkg.getName();

        List<String> segments = new ArrayList<>(Arrays.asList(packageName.split("\\.")));

        // find the LAST segment that is exactly "api" (whole-segment match, not substring)
        int apiIndex = -1;
        for (int i = segments.size() - 1; i >= 0; i--) {
            if (segments.get(i).equals("api")) {
                apiIndex = i;
                break;
            }
        }

        List<String> pathSegments = apiIndex == -1 ? List.of() : segments.subList(apiIndex + 1, segments.size());

        // drop any segment that matches an excluded package name (whole-segment match)
        List<String> filteredSegments = new ArrayList<>();
        for (String segment : pathSegments) {
            if (!packagesToExclude.contains(segment)) {
                filteredSegments.add(segment);
            }
        }

        String basePath = String.join("/", filteredSegments);

        return ("/api/" + basePath + "/").replaceAll("/{2,}", "/");
    }
}
