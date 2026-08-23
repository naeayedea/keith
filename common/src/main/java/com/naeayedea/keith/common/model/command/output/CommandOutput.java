package com.naeayedea.keith.common.model.command.output;

import java.util.List;

/**
 * The result of running a command. Content-shape-specific data (text, a {@code Tile}, ...) lives
 * on the subtype; the defaults here are cross-cutting response actions that apply regardless of
 * content shape, so every {@code ResponseTransformer} implementation can read them the same way.
 */
public interface CommandOutput {

    /**
     * Whether the response should only be visible to the invoking user, where the platform
     * supports it (e.g. a Discord ephemeral interaction reply). Platforms with no such concept
     * should ignore this.
     */
    default boolean isEphemeral() {
        return false;
    }

    /**
     * Generic reaction identifiers (e.g. a unicode emoji) the platform should add to the bot's own
     * response message once sent, if it supports reactions.
     *
     * <p>This is intentionally a bare {@code String} for now rather than a shared type with the
     * reaction-triggered command input model - that model doesn't exist yet. Once it does, this
     * should be reconsidered so both sides describe a reaction the same way instead of drifting
     * apart.
     */
    default List<String> getReactionsToApply() {
        return List.of();
    }
}
