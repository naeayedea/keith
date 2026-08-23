package com.naeayedea.keith.core.api.v1.http.controller.command.channel;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.core.api.annotation.http.KeithHttpCommandController;
import com.naeayedea.keith.core.api.v1.http.internal.model.request.KeithCommandContext;
import com.naeayedea.keith.core.channel.ChannelRef;
import com.naeayedea.keith.core.channel.chat.ChatSessionManager;
import com.naeayedea.keith.core.channel.guess.GuessSessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A leaf app calls this for a plain (non-prefixed) message in a channel it already believes has
 * an active session - see {@code SessionStateEvent}, which is what tells it that in the first
 * place, so this endpoint is never on the hot path for ordinary chat. Checks every channel-scoped
 * session type in turn; whichever owns this channel handles the message.
 *
 * @author naeayedea
 */
@KeithHttpCommandController(baseAccessLevel = AccessLevel.USER)
public class ChannelEvaluateController {

    private final GuessSessionManager guessSessionManager;

    private final ChatSessionManager chatSessionManager;

    public ChannelEvaluateController(
        GuessSessionManager guessSessionManager,
        ChatSessionManager chatSessionManager
    ) {
        this.guessSessionManager = guessSessionManager;
        this.chatSessionManager = chatSessionManager;
    }

    /**
     * @param keithCommandContext who sent the message
     * @param channelId the channel it was sent in
     * @param authorName a display name for the sender, used only if this message ends up relayed
     *                    through an active chat connection
     * @param content the raw message content
     * @return 200 with a response to apply to the sending channel if a session handled it that
     *         way (e.g. a guess result), or 204 if nothing needs to happen there directly (a
     *         chat relay's response goes to the *other* channel, not this one)
     */
    @GetMapping("/evaluate")
    public ResponseEntity<TextResponse> evaluate(
        KeithCommandContext keithCommandContext,
        @RequestParam String channelId,
        @RequestParam String authorName,
        @RequestParam String content
    ) {
        ChannelRef ref = new ChannelRef(keithCommandContext.getPlatform(), channelId);

        if (guessSessionManager.hasSession(ref)) {
            TextResponse response = guessSessionManager.evaluate(ref, content);

            return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
        }

        if (chatSessionManager.hasSession(ref)) {
            chatSessionManager.relay(ref, authorName, content);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.noContent().build();
    }
}
