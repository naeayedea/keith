package succ.commands.admin;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.UserManager;
import java.util.ArrayList;
import java.util.List;

/**
 * UpdateLevel updates a users level to the specified value
 * access only to highest level users, cannot demote a user
 * above or equal to your level.
 */
public class UpdateLevel extends AdminCommand {

    UserManager userManager;
    public UpdateLevel(UserManager um){
        this.userManager = um;
    }
    @Override
    public String getDescription() {
        return "updatelevel: \"updates a users level to the specified value, cannot demote someone above or equal to you.\"";
    }

    @Override
    public void run(MessageReceivedEvent event){String[] messageSplit = event.getMessage().getContentDisplay().split("\\s+");
        int newLevel;
        try{        //Parse the integer from the final argument, return error if not integer
            newLevel = Integer.parseInt(messageSplit[messageSplit.length-1]);
            if(newLevel<0 || newLevel>3){
                event.getChannel().sendMessage("Invalid level! Use values between 0(banned) and 3(admin)!").queue();
                return;
            }
        }
        catch (NumberFormatException e){
            event.getChannel().sendMessage("Incorrect Formatting! Use format: ^admin updatelevel [users] [newlevel]").queue();
            return;
        }
        List<User> users = event.getMessage().getMentionedUsers();  //users contains any users mentioned within the command
        if(users.size()>1){
            event.getChannel().sendMessage("Please only update one user at a time!").queue();
        }
        if(userManager.getUser(event.getAuthor().getId()).getAccessLevel() > userManager.getUser((users.get(0)).getId()).getAccessLevel()){     //If the targetted user has a higher or same rank as author then return error.
            userManager.updateAccessLevel(users.get(0).getId(), newLevel);
            event.getChannel().sendMessage(users.get(0).getAsMention()+" successfully updated to access level "+newLevel).queue();
        }
        else {
            event.getChannel().sendMessage("Unable to update "+users.get(0).getAsMention()+" their level is greater or equal to yours").queue();
        }
    }
}
