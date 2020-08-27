import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.Random;

public class Guess{

    static int number;
    static int numberGuesses = 0;

    public void guess(MessageChannel channel, String message){

        if (message.equals(" start")) {
            System.out.println("guess game initiated");
            channel.sendMessage("Guess a number between 0 and 100: ").queue();
            number = drawNumber();
            numberGuesses = 0;
            System.out.println("Number drawn = "+number);
        }

        else
        guessGame(channel, message.replaceAll("\\s+",""));

        }

    public static void guessGame(MessageChannel channel, String guessRaw) throws NumberFormatException {

        try {
        if (guessRaw.equals("")){
            channel.sendMessage("That is not a valid command. Please type "+MessageHandler.prefix+"help for help.").queue();
            return;
        }
                System.out.println("Number drawn = "+number);


            System.out.println(guessRaw);


            int guess = Integer.parseInt(guessRaw);

            numberGuesses++;

            if (guess == number){

                channel.sendMessage("Congratulations! You guessed correctly! :tada:").queue();
                channel.sendMessage("Completed in " + numberGuesses + " guesses.").queue();

            }

            else {

                if (guess < number) {

                    channel.sendMessage("Number is higher").queue();
                }

                else {

                    channel.sendMessage("Number is lower").queue();
                }

            }
        } catch (NumberFormatException c){
            channel.sendMessage("Please only input integer values!").queue();
        }
    }

    public static int drawNumber(){

        Random randomNumber = new Random();
        int number = randomNumber.nextInt(100);
        return number;
    }
}
