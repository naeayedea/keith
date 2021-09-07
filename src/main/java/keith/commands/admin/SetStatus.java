package keith.commands.admin;

import keith.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Objects;

public class SetStatus extends AdminCommand {

    String defaultName;

    public SetStatus() {
        defaultName = "setstatus";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"sets the bots status to the specified message\"";
    }

    @Override
    public String getLongDescription() {
        return "sets the bot status to the specified message, can also do \"" +
                "setstatus default\" to return the bot status to the default setting";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        JDA jda = Utilities.getJDAInstance();
        if(tokens.size() == 1 && tokens.get(0).equalsIgnoreCase("default")){
            jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));  //Default discord status
            return;
        }
        jda.getPresence().setActivity(Activity.playing(Utilities.stringListToString(tokens)));
    }

    //Only does something if current activity is default
    public static void update(){
        JDA jda = Utilities.getJDAInstance();
        if(Objects.requireNonNull(jda.getPresence().getActivity()).getName().contains("help for commands | ")){
            jda.getPresence().setActivity(Activity.playing("?help for commands | "+jda.getGuilds().size()+ " servers"));  //Default discord status
        }
    }

    public static void set(String newMessage) {
        Utilities.getJDAInstance().getPresence().setActivity(Activity.playing(newMessage));
    }
}
