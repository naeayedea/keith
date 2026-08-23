/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.common.model.api.v1.response.basic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naeayedea.keith.common.model.api.v1.response.AbstractResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TextResponse extends AbstractResponse {

    public static final char USER_REPLACEMENT_CHARACTER = ':';

    private final String text;

    private final Map<String, String> mentionedUsers;

    @JsonCreator
    public TextResponse(@JsonProperty("text") @NonNull String text, @JsonProperty("mentionedUsers") @Nullable Map<String, String> mentionedUsers) {
        this.text = text;
        this.mentionedUsers = mentionedUsers;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @Nullable
    public Map<String, String> getMentionedUsers() {
        return mentionedUsers;
    }

    public char getUserReplacementCharacter() {
        return USER_REPLACEMENT_CHARACTER;
    }

    @NonNull
    public static TextResponse simple(@NonNull String text) {
        return new TextResponse(text, null);
    }

    @NonNull
    public static TextResponseBuilder builder() {
        return new TextResponseBuilder();
    }

    public static final class TextResponseBuilder {

        private final StringBuilder text = new StringBuilder();

        private Map<String, String> mentionedUsers;

        private TextResponseBuilder() {}

        @NonNull
        public TextResponseBuilder addText(@NonNull String text) {
            this.text.append(text.replace(":", "::"));

            return this;
        }

        @NonNull
        public TextResponseBuilder addMentionedUser(@NonNull String userId) {
            if (this.mentionedUsers == null) {
                this.mentionedUsers = new HashMap<>();
            }

            this.text.append(USER_REPLACEMENT_CHARACTER).append(mentionedUsers.size());

            this.mentionedUsers.put("" + mentionedUsers.size(), userId);

            return this;
        }

        @NonNull
        public TextResponse build() {
            return new TextResponse(text.toString(), mentionedUsers);
        }

    }
}
