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
    public String getDescription(MessageReceivedEvent event) {
        return "setstatus: \"sets the bots status to the specified message\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        String messageRaw = event.getMessage().getContentDisplay().trim();
        String[] args = messageRaw.split("\\s+");
        String newActivity = messageRaw.substring(messageRaw.indexOf(args[2]));
        if(newActivity.toLowerCase().equals("default")){
            jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));  //Default discord status
            return;
        }
        jda.getPresence().setActivity(Activity.playing(newActivity));
    }

    //Only does something if current activity is default
    public void update(){
        if(jda.getPresence().getActivity().getName().contains("help for commands | ")){
            jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));  //Default discord status
        }
    }
}
