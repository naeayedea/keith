package com.naeayedea.keith.core.api.v1.http.controller.command.general;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.core.api.annotation.http.KeithHttpCommandController;
import com.naeayedea.keith.core.api.v1.http.internal.model.request.KeithCommandContext;
import com.naeayedea.keith.core.channel.ChannelRef;
import com.naeayedea.keith.core.channel.guess.GuessSessionManager;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Starts a number-guessing game in a channel - see {@link GuessSessionManager} for the actual
 * session lifecycle, and the generic channel-evaluate endpoint for how guesses themselves are
 * scored once a game is running.
 *
 * @author naeayedea
 */
@KeithHttpCommandController(baseAccessLevel = AccessLevel.USER)
public class GuessCommandController {

    private static final int DEFAULT_MAX = 100;

    private static final int UPPER_BOUND = 5000;

    private final GuessSessionManager guessSessionManager;

    public GuessCommandController(GuessSessionManager guessSessionManager) {
        this.guessSessionManager = guessSessionManager;
    }

    @GetMapping("/guess")
    public ResponseEntity<TextResponse> guess(
        KeithCommandContext keithCommandContext,
        @RequestParam String channelId,
        @RequestParam(required = false) @Nullable Integer number
    ) {
        if (number != null && (number < 1 || number > UPPER_BOUND)) {
            return ResponseEntity.ok(TextResponse.simple("Please enter a number between 1 and " + UPPER_BOUND + "."));
        }

        ChannelRef ref = new ChannelRef(keithCommandContext.getPlatform(), channelId);

        return ResponseEntity.ok(guessSessionManager.start(ref, number == null ? DEFAULT_MAX : number));
    }
}
