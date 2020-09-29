package succ.commands.admin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.ServerManager;

public class SetStatus extends AdminCommand{
    private JDA jda;
    private ServerManager serverManager;
    public SetStatus(JDA jda, ServerManager serverManager){
        this.serverManager = serverManager;
        this.jda=jda;
    }
    @Override
    public String getDescription() {
        return "setstatus: \"sets the bots status to the specified message\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        String messageRaw = event.getMessage().getContentDisplay().trim();
        String[] args = messageRaw.split("\\s+");
        String newActivity = messageRaw.substring(messageRaw.indexOf(args[2]));
        jda.getPresence().setActivity(Activity.playing(newActivity));
    }
}
