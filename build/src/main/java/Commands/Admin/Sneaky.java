package Commands.Admin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Sneaky implements AdminCommand{

    /**
     * Sneaky is a command which attempts to give any bot admin a role
     * with server admin if the bot has sufficient permissions.
     */
    public Sneaky(){

    }

    @Override
    public void run(MessageReceivedEvent event){

    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getAccessLevel() {
        return ADMIN;
    }
}
