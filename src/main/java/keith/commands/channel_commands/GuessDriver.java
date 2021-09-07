package keith.commands.channel_commands;

import keith.managers.ChannelCommandManager;
import keith.managers.ServerManager.Server;
import keith.managers.UserManager.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;
import java.util.concurrent.*;

public class GuessDriver implements ChannelCommand {

    private final ScheduledExecutorService timer;
    private ScheduledFuture<?> timerTask;
    private final ChannelCommandManager manager;
    private final MessageChannel channel;
    private final Server server;
    private int answer;
    private int attempts;
    private final int maxNum;

    public GuessDriver(Server server, MessageChannel channel, int maxNum) {
        this.channel = channel;
        this.server = server;
        this.maxNum = maxNum;
        attempts = 0;
        manager = ChannelCommandManager.getInstance();
        timer = Executors.newScheduledThreadPool(1);
        start();
    }

    @Override
    public void evaluate(Message message, List<String> args, User user) {
        attempts++;
        try {
            int guess = Integer.parseInt(args.get(0));
            if (guess == answer) {
                finish();
                message.addReaction("U+1F3C6").queue();
                channel.sendMessage("Congratulations! "+user.getAsMention()+" You guessed correctly in "+attempts+" guesses! :tada:").queue();
                timerTask.cancel(true);
            } else if (guess < answer) {
                message.addReaction("U+2B06").queue();
            } else {
                message.addReaction("U+2B07").queue();
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            //No need to respond
        }
    }

    private void start() {
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
