package succ.commands.drivers;

import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class GuessDriver {


    private MessageChannel channel;
    private int maxNum;
    private static int answer;
    private int attempts;
    public boolean completed;
    public GuessDriver(int number, MessageChannel channel){
        this.channel = channel;
        maxNum = number;
        attempts = 0;
        completed = false;
        drawNumber();
        System.out.println("starting new guess game, answer is "+answer);
    }

    public void guess(int guess){
        attempts++;
        if(guess==answer){
            channel.sendMessage("Congratulations! You guessed correctly in "+attempts+" guesses! :tada:").queue();
            completed = true;
        }
        if(guess<answer){
            channel.sendMessage("Number is hiwer!").queue();
        }
        if(guess>answer){
            channel.sendMessage("Number is lower!").queue();
        }
    }

    public MessageChannel getChannel(){
        return channel;
    }

    private void drawNumber(){
        Random randomNumber = new Random();
        answer = randomNumber.nextInt(maxNum);
    }


}
