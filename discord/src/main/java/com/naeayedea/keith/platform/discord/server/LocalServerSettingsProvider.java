package com.naeayedea.keith.platform.discord.server;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Interim, discord-local stand-in for per-server settings (prefix, ban status).
 *
 * <p>The old {@code KeithServerManager} read/wrote a {@code servers} table that was never carried
 * forward into the v4 database schema (only {@code schema_history} and the user tables exist) -
 * calling it today would throw. Rather than build a real {@code core}-side server endpoint on top
 * of a table that doesn't exist yet, every server gets the default prefix and is never banned. Once
 * server persistence exists in {@code core}, this should be replaced with an HTTP-backed client
 * the same way {@link com.naeayedea.keith.platform.discord.client.CoreUserClient} replaced
 * {@code KeithUserManager} - not extended in place.
 *
 * @author naeayedea
 */
@Component
public class LocalServerSettingsProvider {

    /**
     * The prefix returned for every server until real per-server persistence exists.
     */
    @Value("${keith.default-prefix}")
    private String defaultPrefix;

    @NonNull
    public String getPrefix(@NonNull String serverId) {
        return defaultPrefix;
    }

    public boolean isBanned(@NonNull String serverId) {
        return false;
    }
}
