package com.naeayedea.keith.core.api.v1.http.controller.command.reaction;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.core.api.annotation.http.KeithHttpCommandController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Commands triggered by a user reacting to a message, as opposed to typing one. Unlike the text
 * commands under {@code command/general}, the "input" here is which emoji was used, not message
 * content - see {@code reactionKey}.
 *
 * @author naeayedea
 */
@KeithHttpCommandController(baseAccessLevel = AccessLevel.USER)
public class ReactionCommandController {

    /**
     * Acknowledges a pin reaction. There's no pinned-message persistence yet (no schema for it) -
     * this only proves out reaction-triggered dispatch and the response-level
     * {@link com.naeayedea.keith.common.model.api.v1.response.AbstractResponse#addReaction(String)}
     * mechanism, by asking the platform to add a confirmation reaction distinct from the trigger
     * emoji it already echoes back on its own.
     *
     * @param reactionKey the platform-specific identifier of the emoji that was reacted with
     * @return a {@link TextResponse} carrying a confirmation reaction to apply to the message
     */
    @GetMapping("/pin")
    public ResponseEntity<TextResponse> pin(@RequestParam String reactionKey) {
        TextResponse response = TextResponse.simple("Pinned!");

        response.addReaction("✅");

        return ResponseEntity.ok(response);
    }
}
