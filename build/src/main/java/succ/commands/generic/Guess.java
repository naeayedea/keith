package succ.commands.generic;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.commands.drivers.GuessDriver;
import succ.util.ServerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.*;

public class Guess extends UserCommand{

    private ArrayList<GuessDriver> games;
    private ServerManager serverManager;
    ExecutorService runner;
    private int timeout;
    public Guess(ServerManager serverManager, int timeout){
        this.serverManager = serverManager;
        games = new ArrayList<>();
        runner = Executors.newCachedThreadPool();
        this.timeout = timeout;
    }
    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "guess: \" guess a number between 0 and a number you set! do '"+super.getPrefix(event, serverManager)+"guess [start] [number] to start a new game or [number] to guess!\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        String[] args = event.getMessage().getContentRaw().trim().split("\\s+");
        String prefix = getPrefix(event, serverManager);
        String guess;
        if(args.length > 0) {
            guess = args[1];
        } else {
            channel.sendMessage("Invalid usage, do "+prefix+"guess start [number] or "+prefix+"guess [number] to guess").queue();
            return;
        }
        switch(guess){
            case "start":
            {
                for(GuessDriver game : games){
                    if(game.getChannel().getId().equals(channel.getId())){
                    channel.sendMessage("There is already a game running in this channel! Use "+super.getPrefix(event, serverManager)+"guess [number]").queue();
                    return;
                    }
                }
                try{
                int number = Integer.parseInt(args[2]);
                if(number<=0)
                    throw new NumberFormatException();
                startGame(event, number);
                }
                catch (IndexOutOfBoundsException e){
                    channel.sendMessage("You need to enter a number to guess to! max size is 2147483647!").queue();
                }
                catch (NumberFormatException e){
                    channel.sendMessage("Invalid number! Please enter an integer between 1 and 2147483647").queue();
                }
                break;
            }
            case "end":
            {
                if(!endGame(event, 0))
                    noGameResponse(event);
                break;
            }
            default: //e.g. a guess
            {
                for(GuessDriver game : games){
                    if(game.getChannel().getId().equals(channel.getId())){
                        try{
                            int number = Integer.parseInt(args[1]);
                            game.guess(number, event.getAuthor().getAsMention());
                        }
                        catch (NumberFormatException e){
                            channel.sendMessage("Invalid number! Please enter an integer between 1 and 2147483647").queue();
                        }
                        return;
                    }
                }
                noGameResponse(event);
            }
        }
    }

    @Override
    public int getTimeOut(){
        return 10;
    }

    private void noGameResponse(MessageReceivedEvent event){
        event.getChannel().sendMessage("There is no game in progress! Use "+super.getPrefix(event, serverManager)+"guess start [number] to start a new game!").queue();
    }

    private void startGame(MessageReceivedEvent event, int number){
        new Thread( () -> {
            MessageChannel channel = event.getChannel();
            Runnable newGame = () -> {
                GuessDriver game = new GuessDriver(number, channel);
                games.add(game);
                channel.sendMessage("You have "+timeout+" seconds to guess the number between 0 and "+number+"!").queue();
                while(!game.completed){
                    try {
                        Thread.currentThread().sleep(1);
                    } catch (InterruptedException e) {
                        //do nothing, I should learn how to sync this specific thread with the driver
                    }
                }
                endGame(event, 1);
            };
            try{
                runner.submit(newGame).get(timeout, TimeUnit.SECONDS); //run command operations, kill after max time reached
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                channel.sendMessage("Something went wrong :(").queue();
            } catch (TimeoutException e){
                endGame(event, 0);
            }
        }).start();
    }

    private boolean endGame(MessageReceivedEvent event, int state){
        MessageChannel channel = event.getChannel();
        for(int i=0; i<games.size(); i++){
            GuessDriver game = games.get(i);
            if(game.getChannel().getId().equals(channel.getId())){
                games.remove(game);
                if(state==0)
                    channel.sendMessage("Game ended, the answer was "+game.getAnswer()+"! Use "+super.getPrefix(event, serverManager)+"guess start [number] to start a new game!").queue();
                else
                    //Success let driver send message
                return true;
            }
        }
        return false;
    }
}
