package com.naeayedea.keith.platform.discord.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.naeayedea.keith.platform.discord.utils.KeithDiscordConstants;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The HTTP boundary between this leaf app and {@code core}. Everything discord needs from core -
 * user resolution, running a command - goes through here rather than a direct in-process
 * dependency on core's classes.
 *
 * <p>Uses its own explicitly configured {@link ObjectMapper} (with {@link JavaTimeModule}
 * registered) rather than relying on classpath auto-detection, since this app doesn't pull in
 * {@code spring-boot-starter-web}/{@code -json} and so has no autoconfigured Jackson setup to
 * lean on.
 *
 * @author naeayedea
 */
@Component
public class CoreApiClient {

    /**
     * The underlying HTTP client, pre-configured with a JSR-310-aware Jackson converter.
     */
    private final RestClient restClient;

    public CoreApiClient(@Value("${keith.core.base-url}") String baseUrl) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .messageConverters(converters -> {
                converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
            })
            .build();
    }

    /**
     * Every call is made on behalf of a specific platform user - this bundles that identity in
     * with any endpoint-specific params.
     *
     * @param path the core endpoint path, e.g. {@code /api/v1/command/general/ping}
     * @param platformUserId the Discord id of the user this call is made on behalf of
     * @param extraParams additional query params specific to this endpoint
     * @param responseType the type to deserialize the JSON response body into
     * @return the deserialized response body
     */
    @NonNull
    public <T> T getAsUser(
        @NonNull String path,
        @NonNull String platformUserId,
        @NonNull Map<String, String> extraParams,
        @NonNull Class<T> responseType
    ) {
        Map<String, String> params = new LinkedHashMap<>(extraParams);
        params.put("platform", KeithDiscordConstants.PLATFORM_NAME);
        params.put("platformUserId", platformUserId);

        return restClient.get()
            .uri(uriBuilder -> {
                uriBuilder.path(path);
                params.forEach(uriBuilder::queryParam);
                return uriBuilder.build();
            })
            .retrieve()
            .body(responseType);
    }

    /**
     * @param path the core endpoint path, e.g. {@code /api/v1/command/general/ping}
     * @param platformUserId the Discord id of the user this call is made on behalf of
     * @param responseType the type to deserialize the JSON response body into
     * @return the deserialized response body
     */
    @NonNull
    public <T> T getAsUser(
        @NonNull String path,
        @NonNull String platformUserId,
        @NonNull Class<T> responseType
    ) {
        return getAsUser(path, platformUserId, Map.of(), responseType);
    }
}
