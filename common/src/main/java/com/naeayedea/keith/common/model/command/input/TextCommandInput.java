package com.naeayedea.keith.common.model.command.input;

import com.naeayedea.keith.common.model.message.KeithMessage;

/**
 * The input for a prefix/alias-matched text command. Prefix-less "channel" commands (e.g. an
 * active game session reading unprefixed messages) describe the same shape - a message in a
 * channel - so they reuse this type too; only the dispatch strategy that routes to a handler
 * differs, not the input.
 */
public interface TextCommandInput extends CommandInput {

    KeithMessage getMessage();

}
