package com.naeayedea.keith.core.api.v1.http.controller.command.general;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.core.api.annotation.http.KeithHttpCommandController;
import com.naeayedea.keith.core.api.v1.exception.KeithUserFacingException;
import com.naeayedea.keith.core.api.v1.http.internal.model.request.KeithCommandContext;
import com.naeayedea.keith.core.channel.ChannelRef;
import com.naeayedea.keith.core.channel.chat.ChatMatchmakingManager;
import com.naeayedea.keith.core.channel.chat.ChatSessionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Starts, cancels, or closes an inter-server chat connection - see {@link ChatMatchmakingManager}
 * for pairing and {@link ChatSessionManager} for the active relay itself, which (like guess
 * scoring) happens through the generic channel-evaluate endpoint once a chat is connected.
 *
 * @author naeayedea
 */
@KeithHttpCommandController(baseAccessLevel = AccessLevel.USER)
public class ChatCommandController {

    private final ChatMatchmakingManager matchmakingManager;

    private final ChatSessionManager sessionManager;

    public ChatCommandController(
        ChatMatchmakingManager matchmakingManager,
        ChatSessionManager sessionManager
    ) {
        this.matchmakingManager = matchmakingManager;
        this.sessionManager = sessionManager;
    }

    @GetMapping("/chat")
    public ResponseEntity<TextResponse> chat(
        KeithCommandContext keithCommandContext,
        @RequestParam String channelId,
        @RequestParam String action
    ) throws KeithUserFacingException {
        ChannelRef ref = new ChannelRef(keithCommandContext.getPlatform(), channelId);

        return ResponseEntity.ok(switch (action) {
            case "start" -> matchmakingManager.start(ref);
            case "cancel" -> matchmakingManager.cancel(ref)
                ? TextResponse.simple("Stopped searching for a chat partner.")
                : TextResponse.simple("Not currently searching for a chat partner.");
            case "close" -> sessionManager.close(ref);
            default -> throw new KeithUserFacingException(HttpStatus.BAD_REQUEST, "action must be one of: start, cancel, close");
        });
    }
}
