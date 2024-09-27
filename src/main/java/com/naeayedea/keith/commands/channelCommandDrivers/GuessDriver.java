package com.naeayedea.keith.commands.channelCommandDrivers;

import com.naeayedea.keith.managers.ChannelCommandManager;
import com.naeayedea.keith.model.Candidate;
import com.naeayedea.keith.model.Server;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl;

import java.util.List;
import java.util.concurrent.*;

public class GuessDriver implements ChannelCommandDriver {

    private final ScheduledExecutorService timer;

    private ScheduledFuture<?> timerTask;

    private final ChannelCommandManager manager;

    private final MessageChannel channel;

    private final Server server;

    private int answer;

    private int attempts;

    private final int maxNum;

    private GuessDriver(ChannelCommandManager manager, Server server, MessageChannel channel, int maxNum) {
        this.manager = manager;
        this.channel = channel;
        this.server = server;
        this.maxNum = maxNum;
        attempts = 0;
        timer = Executors.newScheduledThreadPool(1);
    }

    public static GuessDriver driver(ChannelCommandManager manager, Server server, MessageChannel channel, int maxNum) {
        return new GuessDriver(manager, server, channel, maxNum);
    }

    @Override
    public void evaluate(Message message, List<String> args, Candidate candidate) {
        attempts++;
        try {
            int guess = Integer.parseInt(args.get(0));
            if (guess == answer) {
                finish();
                message.addReaction(new UnicodeEmojiImpl("\uD83C\uDF89")).queue();
                channel.sendMessage("Congratulations! "+ candidate.getAsMention()+" You guessed correctly in "+attempts+" guesses! :tada:").queue();
                timerTask.cancel(true);
            } else if (guess < answer) {
                message.addReaction(new UnicodeEmojiImpl("\u2B06")).queue();
            } else {
                message.addReaction(new UnicodeEmojiImpl("\u2B07")).queue();
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            //No need to respond
        }
    }

    public void start() {
        if (manager.gameInProgress(channel.getId())) {
            channel.sendMessage("There is already a game running in this channel! Simply type your guess into chat to play!").queue();
        } else {
            int timeout = 30;
            channel.sendMessage("You have "+ timeout +" seconds to guess the number between 1 and "+maxNum+"!").queue();
            answer = ThreadLocalRandom.current().nextInt(1, maxNum + 1);
            manager.addGame(channel.getId(), this);
            timerTask = timer.schedule(this::finishNoAnswer, timeout, TimeUnit.SECONDS);
            System.out.println("started guess game, answer is "+answer);
        }
    }

    private void finishNoAnswer() {
        channel.sendMessage("Game ended, the answer was "+answer+"! Use "+server.getPrefix()+"guess start [number] to start a new game!").queue();
        finish();
    }

    private void finish() {
        manager.removeGame(channel.getId());
    }

}
