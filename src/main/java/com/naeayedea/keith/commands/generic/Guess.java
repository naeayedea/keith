package com.naeayedea.keith.commands.generic;

import com.naeayedea.keith.commands.channelCommandDrivers.GuessDriver;
import com.naeayedea.keith.managers.ChannelCommandManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.model.Server;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Guess extends AbstractUserCommand {

    private final ServerManager serverManager;

    private final ChannelCommandManager channelCommandManager;

    public Guess(ServerManager serverManager, ChannelCommandManager channelCommandManager) {
        super("guess");

        this.serverManager = serverManager;
        this.channelCommandManager = channelCommandManager;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"number guessing game: use '"+prefix+"guess' or '"+prefix+"guess [number]' to start a game!\"";
    }

    @Override
    public String getLongDescription() {
        return "Guess lets you and your friends to guess the number generated between 1-100 or 1-[number] where [number]"
                +" can be any number between 1-5000! Simply start the game and type your guess in chat to play!";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        Server server = serverManager.getServer(event.getGuild().getId());
        MessageChannel channel = event.getChannel();
        if(tokens.isEmpty()) {
            //start default game between 1 - 100
            GuessDriver.driver(channelCommandManager, server, channel, 100).start();
        } else {
            //attempt to use user input
            try {
                int num = Integer.parseInt(tokens.getFirst());

                if (num < 1 || num > 5000) {
                    throw new NumberFormatException();
                }

                GuessDriver.driver(channelCommandManager, server, channel, num).start();

            } catch (NumberFormatException e) {
                channel.sendMessage("Invalid number! Please enter an integer between 1 and 5000 or do \""+server.getPrefix()+"guess\" to generate a number between 1 - 100").queue();
            }
        }
    }
}
