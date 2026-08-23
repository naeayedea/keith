package com.naeayedea.keith.core.channel.guess;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.event.leaf.OutboundMessageEvent;
import com.naeayedea.keith.common.model.event.leaf.SessionStateEvent;
import com.naeayedea.keith.core.channel.ChannelRef;
import com.naeayedea.keith.core.event.LeafEventPublisher;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Tracks the (at most one) number-guessing game active per channel, the {@code core}-side
 * successor to the old monolith's {@code ChannelCommandManager}/{@code GuessDriver} pair. A game
 * always ends on its own - a correct guess or the timeout - it never runs forever.
 *
 * @author naeayedea
 */
@Component
public class GuessSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(GuessSessionManager.class);

    private static final int TIMEOUT_SECONDS = 30;

    private final Map<ChannelRef, GuessSession> sessions = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;

    private final LeafEventPublisher publisher;

    public GuessSessionManager(
        ScheduledExecutorService sessionTimeoutScheduler,
        LeafEventPublisher publisher
    ) {
        this.scheduler = sessionTimeoutScheduler;
        this.publisher = publisher;
    }

    public boolean hasSession(@NonNull ChannelRef ref) {
        return sessions.containsKey(ref);
    }

    @NonNull
    public TextResponse start(@NonNull ChannelRef ref, int maxNum) {
        if (sessions.containsKey(ref)) {
            return TextResponse.simple("There's already a game running in this channel! Just type your guess to play.");
        }

        int answer = ThreadLocalRandom.current().nextInt(1, maxNum + 1);

        GuessSession session = new GuessSession(answer, maxNum);

        sessions.put(ref, session);

        session.setTimeoutFuture(scheduler.schedule(() -> onTimeout(ref), TIMEOUT_SECONDS, TimeUnit.SECONDS));

        logger.debug("Started guess game in {}, answer is {}", ref, answer);

        publisher.publish(new SessionStateEvent(ref.platform(), ref.channelId(), true));

        return TextResponse.simple("You have " + TIMEOUT_SECONDS + " seconds to guess the number between 1 and " + maxNum + "!");
    }

    /**
     * @param ref the channel the guess was made in
     * @param content the raw message content to try and parse as a guess
     * @return a response to apply to that same channel, or {@code null} if the content wasn't a
     *         valid guess (silently ignored, matching how normal chat in an active game channel
     *         shouldn't be disrupted by gibberish)
     */
    @Nullable
    public TextResponse evaluate(@NonNull ChannelRef ref, @NonNull String content) {
        GuessSession session = sessions.get(ref);

        if (session == null) {
            return null;
        }

        int guess;

        try {
            guess = Integer.parseInt(content.trim());
        } catch (NumberFormatException e) {
            return null;
        }

        int attempts = session.incrementAndGetAttempts();

        if (guess == session.getAnswer()) {
            sessions.remove(ref);
            session.cancelTimeout();

            publisher.publish(new SessionStateEvent(ref.platform(), ref.channelId(), false));

            TextResponse response = TextResponse.simple("Correct! The answer was " + guess + " - you got it in " + attempts + " guesses!");

            response.addReaction("🎉");

            return response;
        }

        TextResponse response = TextResponse.simple("");

        response.addReaction(guess < session.getAnswer() ? "⬆️" : "⬇️");

        return response;
    }

    private void onTimeout(@NonNull ChannelRef ref) {
        GuessSession session = sessions.remove(ref);

        //already finished (won) between the timeout firing and this running - nothing to do
        if (session == null) {
            return;
        }

        logger.debug("Guess game in {} timed out, answer was {}", ref, session.getAnswer());

        publisher.publish(new OutboundMessageEvent(
            ref.platform(),
            ref.channelId(),
            TextResponse.simple("Time's up! The answer was " + session.getAnswer() + ". Start a new game with guess!")
        ));

        publisher.publish(new SessionStateEvent(ref.platform(), ref.channelId(), false));
    }
}
