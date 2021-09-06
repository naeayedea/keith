package keith.commands.generic;

import keith.commands.channel_commands.GuessDriver;
import keith.managers.ServerManager;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Guess extends UserCommand{

    String defaultName;

    public Guess() {
        defaultName = "guess";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"number guessing game: use '"+prefix+"guess' or '"+prefix+"guess [number]' to start a game!\"";
    }

    @Override
    public String getLongDescription() {
        return "Guess lets you and your friends to guess the number generated between 1-100 or 1-[number] where [number]"
                +" can be any number between 1-5000! Simply start the game and type your guess in chat to play!";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        ServerManager.Server server = ServerManager.getInstance().getServer(event.getGuild().getId());
        MessageChannel channel = event.getChannel();
        if(tokens.isEmpty()) {
            //start default game between 1 - 100
            new GuessDriver(server, channel, 100);
        } else {
            //attempt to use user input
            try {
                int num = Integer.parseInt(tokens.get(0));
                if (num < 1 || num > 5000) {
                    throw new NumberFormatException();
                }
                new GuessDriver(server, channel, num);
            } catch (NumberFormatException e) {
                channel.sendMessage("Invalid number! Please enter an integer between 1 and 5000 or do \""+server.getPrefix()+"guess\" to generate a number between 1 - 100").queue();
            }
        }
    }
}
