package com.naeayedea.keith.common.model.command.input;

import com.naeayedea.keith.common.model.message.KeithMessage;
import org.springframework.lang.NonNull;

/**
 * The input for a reaction-triggered command: a user added a reaction to an existing message.
 *
 * <p>{@code getReactionKey()} is intentionally a bare {@code String} identifier (e.g. a unicode
 * emoji, or a platform-specific custom emoji code) rather than a richer type - see the note on
 * {@link com.naeayedea.keith.common.model.command.output.CommandOutput#getReactionsToApply()} for
 * why this should be revisited once both sides of reaction handling exist.
 */
public interface ReactionCommandInput extends CommandInput {

    /**
     * The message the reaction was added to.
     */
    @NonNull
    KeithMessage getTargetMessage();

    /**
     * A generic identifier for the reaction that was added.
     */
    @NonNull
    String getReactionKey();
}
