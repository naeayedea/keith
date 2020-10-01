package succ.commands.generic;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.commands.drivers.GuessDriver;
import succ.util.ServerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

public class Guess extends UserCommand{

    private ArrayList<GuessDriver> games;
    private ServerManager serverManager;
    ExecutorService runner;
    public Guess(ServerManager serverManager){
        this.serverManager = serverManager;
        games = new ArrayList<>();
        runner = Executors.newCachedThreadPool();
    }
    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "guess: \" guess a number between 0 and a number you set! do '"+super.getPrefix(event, serverManager)+"guess [start] [number] to start a new game or [number] to guess!\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        String[] args = event.getMessage().getContentRaw().trim().split("\\s+");
        String guess = args[1];
        System.out.println(Arrays.toString(args));
        switch(guess){
            case "start":
            {
                for(GuessDriver game : games){
                    if(game.getChannel().equals(channel));
                    channel.sendMessage("There is already a game running in this channel! Use "+super.getPrefix(event, serverManager)+"guess [number]").queue();
                    return;
                }
                try{
                int number = Integer.parseInt(args[2]);
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
                endGame(event);
                break;
            }
            default: //e.g. a guess
            {
                for(GuessDriver game : games){
                    if(game.getChannel().equals(channel)){
                        try{
                            int number = Integer.parseInt(args[1]);
                            game.guess(number);
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
        MessageChannel channel = event.getChannel();
        Runnable newGame = () -> {
            GuessDriver game = new GuessDriver(number, channel);
            games.add(game);
            channel.sendMessage("You have 100 seconds to guess the number between 0 and "+number+"!").queue();
            while(!game.completed){
                //Wait for guess
            }
            endGame(event);
        };
        try{
            runner.submit(newGame).get(20, TimeUnit.SECONDS); //run command operations, kill after max time reached

        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            channel.sendMessage("Something went wrong :(").queue();
        } catch (TimeoutException e){
            endGame(event);
        }
    }

    private void endGame(MessageReceivedEvent event){
        MessageChannel channel = event.getChannel();
        for(GuessDriver game : games){
            if(game.getChannel().equals(channel)){
                games.remove(game);
                channel.sendMessage("Game ended! Use "+super.getPrefix(event, serverManager)+"guess start [number] to start a new game!").queue();
                return;
            }
        }
        noGameResponse(event);
    }
}
