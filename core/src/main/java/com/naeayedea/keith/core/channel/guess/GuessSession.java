package com.naeayedea.keith.core.channel.guess;

import java.util.concurrent.ScheduledFuture;

/**
 * The state of one in-progress guessing game: the answer, how many guesses have been made, and
 * the scheduled task that ends the game if nobody guesses it in time.
 *
 * @author naeayedea
 */
public class GuessSession {

    private final int answer;

    private final int maxNum;

    private int attempts;

    private ScheduledFuture<?> timeoutFuture;

    public GuessSession(int answer, int maxNum) {
        this.answer = answer;
        this.maxNum = maxNum;
    }

    public int getAnswer() {
        return answer;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public int incrementAndGetAttempts() {
        return ++attempts;
    }

    public void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) {
        this.timeoutFuture = timeoutFuture;
    }

    public void cancelTimeout() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(true);
        }
    }
}
