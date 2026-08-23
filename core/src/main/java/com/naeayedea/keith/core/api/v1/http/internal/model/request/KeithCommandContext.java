/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.v1.http.internal.model.request;

import org.jspecify.annotations.NonNull;

/**
 * Who is making this request, as asserted by the calling leaf app. {@code platformUserId} is the
 * raw id the platform itself uses (e.g. a Discord snowflake) - it is deliberately kept separate
 * from any resolved, cross-platform canonical user id.
 *
 * <p>Today {@code UserService}/{@code KeithUserManager} still resolve a user by
 * {@code platformUserId} alone, so this is effectively single-platform per user. The database
 * schema already has {@code platform}/{@code user_platform_presence} tables for mapping several
 * platform identities onto one canonical user (e.g. linking a Discord account to a Teams account),
 * but nothing populates or reads them yet - carrying {@code platform} on the wire now means that
 * can be wired up later (alongside a linking flow, likely a challenge-token proving control of the
 * second account) without another breaking change to this contract. Note that today nothing
 * authenticates the leaf app asserting this identity either - that also needs solving before this
 * is exposed beyond a trusted network.
 */
public class KeithCommandContext {

    private final String platform;

    private final String platformUserId;

    private KeithCommandContext(String platform, String platformUserId) {
        this.platform = platform;
        this.platformUserId = platformUserId;
    }

    @NonNull
    public String getPlatform() {
        return platform;
    }

    @NonNull
    public String getPlatformUserId() {
        return platformUserId;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String platform;

        private String platformUserId;

        public Builder platform(@NonNull String platform) {
            this.platform = platform;

            return this;
        }

        public Builder platformUserId(@NonNull String platformUserId) {
            this.platformUserId = platformUserId;

            return this;
        }

        public KeithCommandContext build() throws IllegalStateException {
            validate();

            return new KeithCommandContext(platform, platformUserId);
        }

        private void validate() {
            validateNotBlank(platform, "platform");
            validateNotBlank(platformUserId, "platformUserId");
        }

        private void validateNotBlank(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException(fieldName + " is required");
            }
        }
    }

}
