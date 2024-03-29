package keith.commands.admin;

import keith.commands.AccessLevel;
import keith.managers.UserManager;
import keith.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SetUserLevel extends AdminCommand {

    public SetUserLevel() {
        super("setlevel");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"sets the UserLevel of the specified user\"";
    }

    @Override
    public String getLongDescription() {
        return "sets the UserLevel of the user that corresponds to the entered id or any users that have been tagged" +
                "UserLevels are: 3 (OWNER), 2 (ADMIN), 1 (USER), 0 (BANNED) please use the integer for this command.";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        UserManager.User mentionedUser;
        JDA jda = Utilities.getJDAInstance();
        if (mentionedUsers.isEmpty()) {
            if (tokens.isEmpty()) {
                channel.sendMessage("Invalid Input, please enter a userid/tag someone and an integer corresponding to their level!" +
                        "do help setlevel for more information").queue();
                return;
            } else {
                try {
                User user = jda.getUserById(tokens.get(0));
                if (user != null) {
                    mentionedUser = UserManager.getInstance().getUser(user.getId());
                } else {
                    channel.sendMessage("Could not locate user").queue();
                    return;
                }
                } catch (NumberFormatException e) {
                    channel.sendMessage("Invalid user id format").queue();
                    return;
                }
            }
        } else {
            mentionedUser = UserManager.getInstance().getUser(mentionedUsers.get(0).getId());
        }
        UserManager.User author = UserManager.getInstance().getUser(event.getAuthor().getId());
        try{
            int newLevel = Integer.parseInt(tokens.get(1));
            if (newLevel< 0 || newLevel > 3) {
                event.getChannel().sendMessage("Invalid level! Use values between 0 (Banned) and 3 (Admin)!").queue();
            } else if (author.getAccessLevel().num > newLevel && author.getAccessLevel().num > mentionedUser.getAccessLevel().num) {
                mentionedUser.setAccessLevel(AccessLevel.getLevel(""+newLevel));
                channel.sendMessage("AccessLevel set to "+AccessLevel.getLevel(""+newLevel)).queue();
            } else {
                channel.sendMessage("You do not have the necessary permissions to set this access level!").queue();
            }
        }
        catch (NumberFormatException | IndexOutOfBoundsException e){
            event.getChannel().sendMessage("Incorrect Formatting! Use format: admin updatelevel [user] [newlevel]").queue();
        }
    }
}
