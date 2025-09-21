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

public class KeithCommandContext {

    private final String userId;

    private KeithCommandContext(String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String userId;

        public Builder userId(@NonNull String userId) {
            this.userId = userId;

            return this;
        }

        public KeithCommandContext build() throws IllegalStateException {
            validate();

            return new KeithCommandContext(userId);
        }

        private void validate() {
            validateUserId();
        }

        private void validateUserId() {
            if (userId == null) {
                throw new IllegalStateException("userId is required");
            }

            if (userId.isBlank()) {
                throw new IllegalStateException("userId must not be blank");
            }
        }
    }

}
